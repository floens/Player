package org.floens.player.window;

import android.content.Context;
import android.util.Log;
import android.view.Window;

public class WindowUtils {
    private static final String TAG = "WindowUtils";

    public static Window createWindow(Context context) {
        try {
            Class<?> phoneWindowClass = Class.forName("com.android.internal.policy.PhoneWindow");
            Window window = (Window) phoneWindowClass.getConstructor(Context.class).newInstance(context);

            return window;
        } catch (Exception e) {
            Log.e(TAG, "Could not create window", e);
            return null;
        }
    }
}
