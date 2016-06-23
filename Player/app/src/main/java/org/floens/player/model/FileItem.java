package org.floens.player.model;

import java.io.File;

public class FileItem {
    public File file;

    public FileItem(File file) {
        this.file = file;
    }

    public boolean canNavigate() {
        return file.exists() && file.isDirectory();
    }

    public boolean canOpen() {
        return file.exists() && file.isFile();
    }
}
