package org.floens.mpv;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MpvCore {
    private static final String TAG = "MpvCore";

    static {
        System.loadLibrary("player");
        registerNatives();
    }

    private static native void registerNatives();

    private Handler handler;

    private long propertyObserveCounter = 1;
    private LongSparseArray<PropertyObserver> propertyObservers = new LongSparseArray<>();

    private long eventObserveCounter = 1;
    private Map<String, List<EventObserverHandle>> eventObservers = new HashMap<>();

    private static class EventObserverHandle {
        EventObserver eventObserver;
        long handle;
    }

    public MpvCore() {
        handler = new Handler(Looper.getMainLooper());

        if (nativeInitialize() != 0) {
            throw new RuntimeException("Could not initialize");
        }
    }

    public long observeProperty(PropertyObserver propertyObserver, String name) {
        long userdata = propertyObserveCounter++;
        propertyObservers.append(userdata, propertyObserver);
        nativeObserveProperty(userdata, name);
        return userdata;
    }

    public void unobserveProperty(long userdata) {
        propertyObservers.remove(userdata);
        nativeUnobserveProperty(userdata);
    }

    public long observeEvent(String name, EventObserver eventObserver) {
        EventObserverHandle handle = new EventObserverHandle();
        handle.handle = eventObserveCounter++;
        handle.eventObserver = eventObserver;

        List<EventObserverHandle> forName = eventObservers.get(name);
        if (forName == null) {
            forName = new ArrayList<>();
            eventObservers.put(name, forName);
        }
        forName.add(handle);
        return handle.handle;
    }

    public void unobserveEvent(long handle) {
        for (List<EventObserverHandle> eventObserverHandles : eventObservers.values()) {
            for (int i = 0; i < eventObserverHandles.size(); i++) {
                EventObserverHandle o = eventObserverHandles.get(i);
                if (o.handle == handle) {
                    eventObserverHandles.remove(o);
                    break;
                }
            }
        }
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

    public MpvNode getProperty(String name) {
        return nativeGetProperty(name);
    }

    public void setProperty(String name, MpvNode node) {
        nativeSetProperty(name, node);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // TODO: Destroy what we initialized with nativeInitialize
    }

    private native int nativeInitialize();

    private native int nativeBind();

    private native void nativeResize(int width, int height);

    private native void nativeUnbind();

    private native void nativeCommand(String[] command);

    private native void nativeObserveProperty(long userdata, String name);

    private native void nativeUnobserveProperty(long userdata);

    private native MpvNode nativeGetProperty(String name);

    private native void nativeSetProperty(String name, MpvNode data);

    private void nativeEventNoData(final String eventName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<EventObserverHandle> eventObserverHandles = eventObservers.get(eventName);
                if (eventObserverHandles != null) {
                    for (int i = 0; i < eventObserverHandles.size(); i++) {
                        eventObserverHandles.get(i).eventObserver.onEvent(eventName);
                    }
                }
            }
        });
    }

    private void nativeEventProperty(final long userdata, final MpvProperty property) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                PropertyObserver o = propertyObservers.get(userdata);
                if (o != null) {
                    o.propertyChanged(property);
                } else {
                    Log.e(TAG, "Observer for id " + userdata + " not found");
                }
            }
        });
    }
}
