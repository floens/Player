package org.floens.player.core.model;

import org.floens.player.core.StorageHelper;

import java.io.File;

public class FileItem {
    public File file;

    public FileItem(File file) {
        this.file = file;
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isFolder() {
        return file.isDirectory();
    }

    public boolean canNavigate() {
        return StorageHelper.canNavigate(file);
    }

    public boolean canOpen() {
        return StorageHelper.canOpen(file);
    }
}
