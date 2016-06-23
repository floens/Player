package org.floens.player.core;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FileWatcher {
    private static final String TAG = "FileWatcher";

    private final FileWatcherCallback callback;

    private File currentPath;

    private AFileObserver fileObserver;

    public FileWatcher(FileWatcherCallback callback, File startingPath) {
        this.callback = callback;
        navigateTo(startingPath);
    }

    public void navigateTo(File to) {
        if (!to.exists() || !to.isDirectory()) {
            throw new IllegalArgumentException("Not a directory");
        }

        if (fileObserver != null) {
            fileObserver.stopWatching();
            fileObserver = null;
        }

//        int mask = FileObserver.CREATE | FileObserver.DELETE;
//        fileObserver = new AFileObserver(to.getAbsolutePath(), mask);
        String canonicalPath = null;
        try {
            canonicalPath = to.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        fileObserver = new AFileObserver("/sdcard/");
//        fileObserver.startWatching();

        currentPath = to;

        File[] files = currentPath.listFiles();

        List<File> fileList = Arrays.asList(files);
        callback.onFiles(fileList);
    }

    private class AFileObserver extends FileObserver {
        public AFileObserver(String path) {
            super(path);
        }

        public AFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            Log.d(TAG, "onEvent() called with: " + "event = [" + event + "], path = [" + path + "]");
        }
    }

    public interface FileWatcherCallback {
        void onFiles(List<File> files);
    }
}
