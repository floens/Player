package org.floens.player.controller;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import org.floens.controller.AndroidUtils;
import org.floens.controller.Controller;
import org.floens.controller.StartActivity;
import org.floens.controller.permissions.RuntimePermissionsHelper;
import org.floens.controller.transition.FadeInTransition;
import org.floens.player.InsetsHelper;
import org.floens.player.R;
import org.floens.player.adapter.FilesAdapter;
import org.floens.player.core.FileWatcher;
import org.floens.player.layout.FilesLayout;
import org.floens.player.model.FileItem;
import org.floens.player.model.FileItems;

public class FilesController extends Controller implements FileWatcher.FileWatcherCallback, FilesAdapter.Callback, FilesLayout.Callback {
    private static final String TAG = "FilesController";

    private FilesLayout filesLayout;

    private RuntimePermissionsHelper runtimePermissionsHelper;
    private boolean gotPermission = false;

    private FileWatcher fileWatcher;
    private FileItems fileItems;

    public FilesController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        bottomBarItem.setText(R.string.item_files);
        bottomBarItem.drawable = context.getResources().getDrawable(R.drawable.ic_folder_open_black_24dp, null);

        filesLayout = (FilesLayout) inflateRes(R.layout.layout_files);
        view = filesLayout;
        filesLayout.setCallback(this);

        InsetsHelper.attachInsetsMargin(filesLayout.getBackLayout(), false, true, false, false);
        InsetsHelper.attachInsetsMargin(filesLayout.getStorageText(), false, true, false, false);

        fileWatcher = new FileWatcher(this, Environment.getExternalStorageDirectory());

        runtimePermissionsHelper = ((StartActivity) context).getRuntimePermissionsHelper();
        gotPermission = hasPermission();
        if (gotPermission) {
            initialize();
        } else {
            requestPermission();
        }
    }

    @Override
    public boolean onBack() {
        if (fileItems != null && fileItems.canNavigateUp) {
            fileWatcher.navigateUp();
            return true;
        } else {
            return super.onBack();
        }
    }

    @Override
    public void onFiles(FileItems fileItems) {
        this.fileItems = fileItems;
        filesLayout.setFiles(fileItems);
    }

    @Override
    public void onBackClicked() {
        fileWatcher.navigateUp();
    }

    @Override
    public void onFileItemClicked(FileItem fileItem) {
        if (fileItem.canNavigate()) {
            fileWatcher.navigateTo(fileItem.file);
        } else if (fileItem.canOpen()) {
//            Uri uri = Uri.fromFile(fileItem.file);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.setDataAndType(uri, "image/*");
//            AndroidUtils.openIntent(intent);

            PlayerController playerController = new PlayerController(context);
            playerController.setFileItem(fileItem);
            navigationController.pushController(playerController, new FadeInTransition());
        }
    }

    @Override
    public void onStorageClicked(FileItem fileItem) {
        if (fileItem.canNavigate()) {
            fileWatcher.navigateTo(fileItem.file);
        }
    }

    private boolean hasPermission() {
        return runtimePermissionsHelper.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermission() {
        runtimePermissionsHelper.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new RuntimePermissionsHelper.Callback() {
            @Override
            public void onRuntimePermissionResult(boolean granted) {
                gotPermission = granted;
                if (gotPermission) {
                    initialize();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.write_permission_required_title)
                            .setMessage(R.string.write_permission_required)
                            .setCancelable(false)
                            .setNeutralButton(R.string.write_permission_app_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermission();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:" + context.getPackageName()));
                                    AndroidUtils.openIntent(intent);
                                }
                            })
                            .setPositiveButton(R.string.write_permission_grant, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermission();
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void initialize() {
        filesLayout.initialize();
        fileWatcher.initialize();
    }
}
