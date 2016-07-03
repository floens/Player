package org.floens.player.mpv;

public class MpvCore {
    private static final String TAG = "MpvCore";

    public MpvCore() {
        loadLibraries();
    }

    private void loadLibraries() {
        System.loadLibrary("player");

        if (native_initialize() != 0) {
            throw new RuntimeException("Could not initialize");
        }
    }

    private native int native_initialize();
}
