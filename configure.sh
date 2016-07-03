#!/bin/bash -e

fribidi_version=0.19.7
lua_version=5.2.4
lua_version_int=52
android_version=23

ORIGINAL_PATH=$PATH

log_info() {
    echo -e "\033[1;36m$1\033[0m"
}

log_error() {
    echo -e "\033[1;31m$1\033[0m"
}

pushds() {
    pushd $1 >/dev/null
}

popds() {
    popd >/dev/null
}

if [[ -z $ANDROID_HOME ]]; then
    log_error '$ANDROID_HOME is not set'
    exit 1
fi

if [[ -z $ANDROID_NDK ]]; then
    log_error '$ANDROID_NDK is not set'
    exit 1
fi

gather_deps() {
    mkdir -p deps
    cd deps

    if [[ ! -d "ffmpeg" ]]; then
        log_info "Setting up ffmpeg"
        git clone https://git.ffmpeg.org/ffmpeg.git ffmpeg
    fi

    if [[ ! -d freetype2 ]]; then
        log_info "Setting up freetype2"
        git clone git://git.sv.nongnu.org/freetype/freetype2.git freetype2
    fi

    if [[ ! -d fribidi ]]; then
        log_info "Setting up fribidi"
        git clone git://anongit.freedesktop.org/fribidi/fribidi fribidi
        cd fribidi
        git checkout $fribidi_version
        ./bootstrap
        patch gen.tab/Makefile.am ../../patches/fribidi_gen_tab_host_cc_hack.patch
        cd ..
    fi

    if [[ ! -d libass ]]; then
        log_info "Setting up libass"
        git clone https://github.com/libass/libass libass
    fi

    if [[ ! -d lua ]]; then
        log_info "Setting up lua"
        mkdir lua
        cd lua
        wget https://www.lua.org/ftp/lua-$lua_version.tar.gz -O - | tar -xz -f - --strip-components=1
        cd ..
    fi

    if [[ ! -d mpv ]]; then
        log_info "Setting up mpv"
        git clone https://github.com/mpv-player/mpv mpv
    fi

    cd ..
}

configure_toolchain() {
    local arch=$1
    local toolchain_dir=toolchain/$arch

    if [[ ! -d $toolchain_dir ]]; then
        log_info "Configuring toolchain for $arch"
        $ANDROID_NDK/build/tools/make_standalone_toolchain.py \
            --arch=$arch --api=$android_version --install-dir=$toolchain_dir
    fi
}

# 'arm': 'arm-linux-androideabi',
# 'arm64': 'aarch64-linux-android',
# 'x86': 'i686-linux-android',
# 'x86_64': 'x86_64-linux-android',
# 'mips': 'mipsel-linux-android',
# 'mips64': 'mips64el-linux-android',

declare -A host_arches
host_arches=(
    [arm]=arm-linux-androideabi
    [arm64]=aarch64-linux-android
    [x86]=i686-linux-android
    [x86_64]=x86_64-linux-android
)

configure_freetype2() {
    local host=$1
    local prefix_dir=$2

    pushds deps/freetype2
    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring freetype2"

        [[ -f builds/unix/configure ]] || ./autogen.sh
        PKG_CONFIG=/bin/false \
            ./configure \
            --host=$host \
            --enable-static --disable-shared \
            --with-png=no \
            --prefix=$prefix_dir

        make clean

        echo $host > configured_for
    fi
    popds
}

build_freetype2() {
    log_info "Building freetype2"

    pushds deps/freetype2
    make -j$(nproc) --no-print-directory
    make INSTALL="install -p" install >/dev/null
    popds
}

configure_fribidi() {
    local host=$1
    local prefix_dir=$2

    pushds deps/fribidi

    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring fribidi"

        PKG_CONFIG=/bin/false \
        ./configure \
            --host=$host \
            --enable-static --disable-shared \
            --prefix=$prefix_dir

        make clean

        echo $host > configured_for
    fi
    popds
}

build_fribidi() {
    log_info "Building fribidi"

    pushds deps/fribidi
    make -j1 --no-print-directory # note: using more than 1 concurrent tasks breaks the build
    make INSTALL="install -p" install >/dev/null
    popds
}

configure_libass() {
    local host=$1
    local prefix_dir=$2

    pushds deps/libass
    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring libass"

        [[ -f configure ]] || ./autogen.sh
        PKG_CONFIG_LIBDIR="$prefix_dir/lib/pkgconfig" \
        ./configure \
            --host=$host \
            --disable-require-system-font-provider \
            --enable-static --disable-shared \
            --prefix=$prefix_dir

        make clean

        echo $host > configured_for
    fi
    popds
}

build_libass() {
    log_info "Building libass"

    pushds deps/libass
    make -j$(nproc) --no-print-directory
    make install >/dev/null
    popds
}

configure_lua() {
    local host=$1
    local prefix_dir=$2

    pushds deps/lua
    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring lua"

        # Nothing to configure because we set the compiler at the build stage
        make clean

        echo $host > configured_for
    fi
    popds
}

build_lua() {
    local host=$1
    local prefix_dir=$2

    log_info "Building lua"

    pushds deps/lua

    # -Dgetlocaledecpoint()=('.') fixes bionic missing decimal_point in localeconv
    # LUA_T= and LUAC_T= disable building lua & luac
    make CC="$host-gcc -Dgetlocaledecpoint\(\)=\(\'.\'\)" \
        PLAT=linux \
        AR="$host-ar r" \
        RANLIB="$host-ranlib" \
        LUA_T= \
        LUAC_T= \
        -j$(nproc) \
        --no-print-directory

    # INSTALL_EXEC=: disables installing lua & luac
    make INSTALL_TOP=$prefix_dir \
        INSTALL_EXEC=: \
        install >/dev/null

    # Install pkgconfig file
    make INSTALL_TOP=$prefix_dir \
        pc > $prefix_dir/lib/pkgconfig/lua.pc

    # the pkgconfig file is incomplete, fix it
    echo -e "\nName: Lua\nDescription:\nVersion: ${lua_version}" >> $prefix_dir/lib/pkgconfig/lua.pc
    echo -e 'Libs: -L${libdir} -llua\nCflags: -I${includedir}' >> $prefix_dir/lib/pkgconfig/lua.pc

    popds
}

configure_ffmpeg() {
    local host=$1
    local prefix_dir=$2
    local arch=$3

    pushds deps/ffmpeg
    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring ffmpeg for arch $arch"

        # TODO: optimize with --cpu ?
        # --cpu=armv7-a
        ./configure \
            --target-os=android \
            --enable-cross-compile \
            --cross-prefix=$host- \
            --arch=$arch \
            --enable-jni \
            --enable-mediacodec \
            --enable-static \
            --disable-shared \
            --prefix=$prefix_dir \
            --disable-debug \
            --disable-doc \
            --enable-gpl \
            --disable-encoders \
            --disable-programs

        make clean

        echo $host > configured_for
    fi
    popds
}

build_ffmpeg() {
    log_info "Building ffmpeg"

    pushds deps/ffmpeg
    make -j$(nproc)
    make install >/dev/null
    popds
}

configure_mpv() {
    local host=$1
    local prefix_dir=$2

    pushds deps/mpv
    # if not already configured for this host
    if ! grep -q $host configured_for; then
        log_info "Configuring mpv"

        [[ -f waf ]] || ./bootstrap.py

        CC=$host-gcc PKG_CONFIG_LIBDIR="$prefix_dir/lib/pkgconfig" \
            ./waf configure \
            --disable-iconv \
            --lua=$lua_version_int \
            --enable-libmpv-static \
            --enable-static-build \
            --prefix=$prefix_dir \
            --disable-manpage-build \
            --enable-android \
            --enable-lua \
            --disable-cplayer

        ./waf clean

        echo $host > configured_for
    fi
    popds
}

build_mpv() {
    local prefix_dir=$1

    log_info "Building mpv"

    pushds deps/mpv

    ./waf build -p -j$(nproc)
    ./waf install #>/dev/null

    popds
}

configure_deps() {
    local arch=$1

    local host=${host_arches[$arch]}
    [[ -z $host ]] && log_error "Unsupported arch $arch" && exit 1

    log_info "Configuring dependencies for arch $arch"

    local toolchain_dir=$(readlink -f toolchain/$arch)
    local prefix_dir=$(readlink -f prefix/$arch)
    mkdir -p $prefix_dir

    PATH="$toolchain_dir/bin:$ORIGINAL_PATH"

    configure_freetype2 $host $prefix_dir
    build_freetype2

    configure_fribidi $host $prefix_dir
    build_fribidi

    configure_libass $host $prefix_dir
    build_libass

    configure_lua $host $prefix_dir
    build_lua $host $prefix_dir

    configure_ffmpeg $host $prefix_dir $arch
    build_ffmpeg

    configure_mpv $host $prefix_dir
    build_mpv $prefix_dir
}

copy_libs() {
    local arch=$1
    local prefix_dir=prefix/$arch

    local applibs=Player/app/libs

    # local libdest=$applibs/$arch
    local libdest=$applibs/arm64-v8a
    mkdir -p $libdest

    cp $prefix_dir/lib/libmpv.a $libdest/
    cp $prefix_dir/lib/liblua.a $libdest/
    cp $prefix_dir/lib/libavfilter.a $libdest/
    cp $prefix_dir/lib/libavutil.a $libdest/
    cp $prefix_dir/lib/libavformat.a $libdest/
    cp $prefix_dir/lib/libavcodec.a $libdest/
    cp $prefix_dir/lib/libavdevice.a $libdest/
    cp $prefix_dir/lib/libswscale.a $libdest/
    cp $prefix_dir/lib/libswresample.a $libdest/
    cp $prefix_dir/lib/libpostproc.a $libdest/
    cp $prefix_dir/lib/libass.a $libdest/
    cp $prefix_dir/lib/libfreetype.a $libdest/
    cp $prefix_dir/lib/libfribidi.a $libdest/

    cp -r $prefix_dir/include $applibs
}

# arm,arm64,mips,mips64,x86,x86_64
arches=$1
if [[ -z $arches ]]; then
    arches="arm64"
fi

for i in $arches; do
    [[ -z ${host_arches[$i]} ]] && log_error "Unsupported architecture $i" && exit 1
done

gather_deps

for i in $arches; do
    configure_toolchain $i
done

for i in $arches; do
    configure_deps $i
    copy_libs $i
done
