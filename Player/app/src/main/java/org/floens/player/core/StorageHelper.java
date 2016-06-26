package org.floens.player.core;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import org.floens.controller.AndroidUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    private static final String TAG = "StorageHelper";

    private static List<Storage> storage;

    public static class Storage {
        public File file;
        public String description;

        private Storage(File file, String description) {
            this.file = file;
            this.description = description;
        }
    }

    static {
        // Get the external storage with reflection first to get the path & description
        boolean gotStorageWithReflection = false;

        Context context = AndroidUtils.getAppContext();

        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Method getVolumeList = null;
            Class<StorageManager> storageManagerClass = StorageManager.class;
            for (Method method : storageManagerClass.getMethods()) {
                if (method.getName().equals("getVolumeList") && !Modifier.isStatic(method.getModifiers())) {
                    getVolumeList = method;
                }
            }

            Class<?> storageVolume = Class.forName("android.os.storage.StorageVolume");

            Field storageVolumePath = storageVolume.getDeclaredField("mPath");
            storageVolumePath.setAccessible(true);
            Method storageVolumeDescription = storageVolume.getDeclaredMethod("getDescription", Context.class);
            Method storageVolumeState = storageVolume.getDeclaredMethod("getState");
            storageVolumeDescription.setAccessible(true);

            List<Storage> items = new ArrayList<>();

            if (getVolumeList != null) {
                Object[] volumes = (Object[]) getVolumeList.invoke(storageManager);

                for (Object volume : volumes) {
                    File file = (File) storageVolumePath.get(volume);
                    String description = (String) storageVolumeDescription.invoke(volume, context);
                    String state = (String) storageVolumeState.invoke(volume);
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        items.add(new Storage(file, description));
                    }
                }

                storage = items;
                gotStorageWithReflection = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting volumes with reflection", e);
        }

        // use the safe approach if the reflection failed
        if (!gotStorageWithReflection) {
            storage = new ArrayList<>();

            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            for (File f : externalFilesDirs) {
                // Go up from Android/data/<packagename>/data
                File real = f;
                for (int i = 0; i < 4; i++) {
                    File parent = real.getParentFile();
                    if (parent != null && canNavigate(parent)) {
                        real = parent;
                    } else {
                        break;
                    }
                }

                String name = real.getName();
                if (name.equals("0")) {
                    name = "Internal storage";
                }
                storage.add(new Storage(real, name));
            }
        }
    }

    public static List<Storage> externalFolders(Context context) {
        return storage;
    }

    public static boolean canNavigate(File file) {
        return file != null && !isDirectoryBlacklisted(file) && file.exists()
                && file.isDirectory() && file.canRead();
    }

    public static boolean isDirectoryBlacklisted(File file) {
        String absolutePath = file.getAbsolutePath();
        switch (absolutePath) {
            case "/storage":
                return true;
            case "/storage/emulated":
                return true;
            case "/storage/emulated/0/0":
                return true;
            case "/storage/emulated/legacy":
                return true;
        }
        return false;
    }

    public static boolean canOpen(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }
}
