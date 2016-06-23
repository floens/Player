package org.floens.player.controller;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;

import org.floens.controller.Controller;
import org.floens.player.InsetsHelper;
import org.floens.player.R;
import org.floens.player.adapter.FilesAdapter;
import org.floens.player.core.FileWatcher;
import org.floens.player.layout.FilesLayout;
import org.floens.player.model.FileItem;

import java.io.File;
import java.util.List;

public class FilesController extends Controller implements FileWatcher.FileWatcherCallback, FilesAdapter.Callback {
    private FilesLayout filesLayout;

    private FilesAdapter filesAdapter;

    private FileWatcher fileWatcher;

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
        InsetsHelper.attachInsetsPadding(filesLayout.getRecyclerView(), false, true, false, true);

        filesLayout.getRecyclerView().setLayoutManager(new LinearLayoutManager(context));

        filesAdapter = new FilesAdapter(this);
        filesLayout.getRecyclerView().setAdapter(filesAdapter);

        fileWatcher = new FileWatcher(this, Environment.getExternalStorageDirectory());
    }

    @Override
    public void onFiles(List<File> files) {
        filesAdapter.setFiles(files);
    }

    @Override
    public void onFileItemClicked(FileItem fileItem) {
        if (fileItem.canNavigate()) {
            fileWatcher.navigateTo(fileItem.file);
        }
    }
}
