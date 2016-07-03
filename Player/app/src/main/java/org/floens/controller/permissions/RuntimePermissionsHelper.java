package org.floens.controller.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static org.floens.controller.AndroidUtils.getAppContext;

public class RuntimePermissionsHelper {
    private static final int RUNTIME_PERMISSION_RESULT_ID = 100;

    private ActivityCompat.OnRequestPermissionsResultCallback callbackActvity;

    private CallbackHolder pendingCallback;

    public RuntimePermissionsHelper(ActivityCompat.OnRequestPermissionsResultCallback callbackActvity) {
        this.callbackActvity = callbackActvity;
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getAppContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean requestPermission(String permission, Callback callback) {
        if (pendingCallback == null) {
            pendingCallback = new CallbackHolder();
            pendingCallback.callback = callback;
            pendingCallback.permission = permission;

            ActivityCompat.requestPermissions((Activity) callbackActvity, new String[]{permission}, RUNTIME_PERMISSION_RESULT_ID);

            return true;
        } else {
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RUNTIME_PERMISSION_RESULT_ID && pendingCallback != null) {
            boolean granted = false;

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (permission.equals(pendingCallback.permission) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            pendingCallback.callback.onRuntimePermissionResult(granted);
            pendingCallback = null;
        }
    }

    private class CallbackHolder {
        private Callback callback;
        private String permission;
    }

    public interface Callback {
        void onRuntimePermissionResult(boolean granted);
    }
}
