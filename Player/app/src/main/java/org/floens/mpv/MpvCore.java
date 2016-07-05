package org.floens.mpv;

public class MpvCore {
    private static final String TAG = "MpvCore";

    public MpvCore() {
        loadLibraries();
    }

    public void bind(int width, int height) {
        if (nativeBind() != 0) {
            throw new RuntimeException("Could not bind");
        }
        nativeResize(width, height);
    }

    public void resize(int width, int height) {
        nativeResize(width, height);
    }

    public void unbind() {
        nativeUnbind();
    }

    public void command(String[] command) {
        nativeCommand(command);
    }

    private void loadLibraries() {
        System.loadLibrary("player");

        if (nativeInitialize() != 0) {
            throw new RuntimeException("Could not initialize");
        }
    }

    private native int nativeInitialize();

    private native int nativeBind();

    private native void nativeResize(int width, int height);

    private native void nativeUnbind();

    private native void nativeCommand(String[] command);
}
