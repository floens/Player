package org.floens.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import org.floens.controller.permissions.RuntimePermissionsHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class StartActivity extends AppCompatActivity {
    protected Controller mainController;

    private ViewGroup contentView;
    private List<Controller> stack = new ArrayList<>();

    private RuntimePermissionsHelper runtimePermissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runtimePermissionsHelper = new RuntimePermissionsHelper(this);

        contentView = (ViewGroup) findViewById(android.R.id.content);

        mainController.onCreate();
        mainController.onShow();

        setContentView(mainController.view);
        addController(mainController);

        // Prevent overdraw
        // Do this after setContentView, or the decor creating will reset the background to a default non-null drawable
        getWindow().setBackgroundDrawable(null);
    }

    public RuntimePermissionsHelper getRuntimePermissionsHelper() {
        return runtimePermissionsHelper;
    }

    public void addController(Controller controller) {
        stack.add(controller);
    }

    public void removeController(Controller controller) {
        stack.remove(controller);
    }

    public ViewGroup getContentView() {
        return contentView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (Controller controller : stack) {
            controller.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        for (Controller controller : stack) {
            controller.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onBackPressed() {
        if (!stackTop().onBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stackTop().onHide();
        stackTop().onDestroy();
        stack.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        MusicApplication.getInstance().activityEnteredForeground();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        MusicApplication.getInstance().activityEnteredBackground();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        runtimePermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Controller stackTop() {
        return stack.get(stack.size() - 1);
    }
}
