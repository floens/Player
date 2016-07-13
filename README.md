# Player - video player for Android based on mpv

Player is a work in progress video player for Android based on mpv.

It's working as it currently is, but not finished yet.

## Building
Building the native libs works on Linux.  
Set the ANDROID_HOME env var to your Android sdk directory and ANDROID_NDK to the ndk-bundle folder inside the ndk. ndk r12 or higher required.

To support all Android architectures with its toolchains a build script was made. The configure.sh script is the only build script right now. It sets up the dependencies and builds them. Re-running this script only builds dependencies that need building.

The second argument of the build script is the architecture it should build, if you supply none then the architecture is "arm64".  
The dependencies will automatically reconfigure themselves to the new toolchain if you change the argument of the configure script. This works for all dependencies except for ffmpeg, because it doesn't clear all object files when asking it to clean. This is a bug that will be fixed later, just remove the ffmpeg folder when changing architectures for now.

## License
This project is licensed under the GPLv2 or later. This has not been permanently decided yet and might change.
