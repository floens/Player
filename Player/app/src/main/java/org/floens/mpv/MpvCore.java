package org.floens.mpv;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LongSparseArray;

public class MpvCore {
    private static final String TAG = "MpvCore";

    static {
        System.loadLibrary("player");
        registerNatives();
    }

    private static native void registerNatives();

    private Handler handler;

    private long propertyObserveCounter = 1;
    private LongSparseArray<PropertyObserver> observers = new LongSparseArray<>();

    public MpvCore() {
        if (nativeInitialize() != 0) {
            throw new RuntimeException("Could not initialize");
        }
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // TODO: Destroy what we initialized with nativeInitialize
    }

    public long observeProperty(PropertyObserver observer, String name, int format) {
        long userdata = propertyObserveCounter++;
        observers.append(userdata, observer);
        nativeObserveProperty(userdata, name, format);
        return userdata;
    }

    public void unobserveProperty(long userdata) {
        observers.remove(userdata);
        nativeUnobserveProperty(userdata);
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

    private native int nativeInitialize();

    private native int nativeBind();

    private native void nativeResize(int width, int height);

    private native void nativeUnbind();

    private native void nativeCommand(String[] command);

    private native void nativeObserveProperty(long userdata, String name, int format);

    private native void nativeUnobserveProperty(long userdata);

    private void nativeEventNoData(String eventName) {
        Log.d(TAG, "nativeEventNoData() called with: eventName = [" + eventName + "]");
    }

    private void nativeEventProperty(final long userdata, final MpvProperty property) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                PropertyObserver o = observers.get(userdata);
                if (o != null) {
                    o.propertyChanged(property);
                } else {
                    Log.e(TAG, "Observer for id " + userdata + " not found");
                }
            }
        });
    }
}
