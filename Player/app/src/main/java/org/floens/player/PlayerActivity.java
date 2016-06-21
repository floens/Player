package org.floens.player;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.floens.controller.NavigationController;
import org.floens.controller.StartActivity;
import org.floens.player.controller.MainNavigationController;
import org.floens.player.controller.PagedNavigationController;
import org.floens.player.controller.PlaylistController;

public class PlayerActivity extends StartActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        window.setAttributes(attributes);

//        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mainController = new MainNavigationController(this);

        super.onCreate(savedInstanceState);

        NavigationController mainController = (NavigationController) this.mainController;
        PagedNavigationController pagedNavigationController = new PagedNavigationController(this);
        mainController.pushController(pagedNavigationController, false);

        for (int i = 0; i < 3; i++) {
            pagedNavigationController.addController(new PlaylistController(this));
        }
    }
}
