package org.floens.player;

import android.app.Application;
import android.os.StrictMode;

import org.floens.controller.utils.AndroidUtils;
import org.floens.mpv.MpvCore;

public class PlayerApplication extends Application {
    private static PlayerApplication instance;

    private MpvCore mpvCore;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AndroidUtils.setApplicationContext(this);

        mpvCore = new MpvCore();

        // Initialize after the heavy lifting is done
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
    }

    public MpvCore getMpvCore() {
        return mpvCore;
    }

    public static PlayerApplication getInstance() {
        return instance;
    }
}
