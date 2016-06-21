package org.floens.player;

import android.app.Application;

import org.floens.controller.AndroidUtils;

public class PlayerApplication extends Application {
    private static PlayerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        AndroidUtils.setApplicationContext(this);
    }

    public static PlayerApplication getInstance() {
        return instance;
    }
}
