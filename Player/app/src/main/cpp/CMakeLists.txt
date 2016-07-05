cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

# app/libs
set(libs_dir ${CMAKE_SOURCE_DIR}/../../../libs)

add_library(lib_fribidi STATIC IMPORTED)
set_target_properties(lib_fribidi PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libfribidi.a)

add_library(lib_freetype STATIC IMPORTED)
set_target_properties(lib_freetype PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libfreetype.a)

add_library(lib_ass STATIC IMPORTED)
set_target_properties(lib_ass PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libass.a)

add_library(lib_swresample STATIC IMPORTED)
set_target_properties(lib_swresample PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libswresample.a)

add_library(lib_swscale STATIC IMPORTED)
set_target_properties(lib_swscale PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libswscale.a)

add_library(lib_avdevice STATIC IMPORTED)
set_target_properties(lib_avdevice PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libavdevice.a)

add_library(lib_avcodec STATIC IMPORTED)
set_target_properties(lib_avcodec PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libavcodec.a)

add_library(lib_avformat STATIC IMPORTED)
set_target_properties(lib_avformat PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libavformat.a)

add_library(lib_avutil STATIC IMPORTED)
set_target_properties(lib_avutil PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libavutil.a)

add_library(lib_avfilter STATIC IMPORTED)
set_target_properties(lib_avfilter PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libavfilter.a)

add_library(lib_postproc STATIC IMPORTED)
set_target_properties(lib_postproc PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libpostproc.a)

add_library(lib_lua STATIC IMPORTED)
set_target_properties(lib_lua PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/liblua.a)

add_library(lib_mpv STATIC IMPORTED)
set_target_properties(lib_mpv PROPERTIES IMPORTED_LOCATION ${libs_dir}/${ANDROID_ABI}/libmpv.a)

include_directories(${libs_dir}/include)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c11")

add_library(player SHARED
    event_thread.c render_thread.c player_interface.c logger.c player.c
)

target_link_libraries(
    player
    android log lib_mpv
    OpenSLES GLESv3 EGL lib_lua
    lib_swresample lib_avfilter lib_postproc lib_avformat lib_avcodec lib_swscale lib_avdevice lib_avutil
    lib_ass lib_freetype lib_fribidi
    m z
)
