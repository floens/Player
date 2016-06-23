package org.floens.player;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.floens.controller.NavigationController;
import org.floens.controller.StartActivity;
import org.floens.player.controller.FilesController;
import org.floens.player.controller.MainNavigationController;
import org.floens.player.controller.PagedNavigationController;
import org.floens.player.controller.PlaylistController;

public class PlayerActivity extends StartActivity {
    private static final String TAG = "PlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        window.setAttributes(attributes);

        NavigationController mainController = new MainNavigationController(this);
        this.mainController = mainController;

        super.onCreate(savedInstanceState);

        mainController.view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        PagedNavigationController pagedNavigationController = new PagedNavigationController(this);
        mainController.pushController(pagedNavigationController, false);

        pagedNavigationController.addController(new FilesController(this));
        pagedNavigationController.addController(new PlaylistController(this));
        pagedNavigationController.addController(new PlaylistController(this));
    }
}
