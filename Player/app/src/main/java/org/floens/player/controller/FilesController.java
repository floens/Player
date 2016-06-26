package org.floens.player.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import org.floens.controller.AndroidUtils;
import org.floens.controller.Controller;
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
            Uri uri = Uri.fromFile(fileItem.file);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setDataAndType(uri, "image/*");
            AndroidUtils.openIntent(intent);
        }
    }

    @Override
    public void onStorageClicked(FileItem fileItem) {
        if (fileItem.canNavigate()) {
            fileWatcher.navigateTo(fileItem.file);
        }
    }
}
