package org.floens.player.controller;

import android.content.Context;

import org.floens.controller.NavigationController;
import org.floens.controller.ui.layout.NavigationControllerContainerLayout;
import org.floens.player.R;

public class MainNavigationController extends NavigationController {
    public MainNavigationController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        view = inflateRes(R.layout.controller_navigation);
        NavigationControllerContainerLayout container = (NavigationControllerContainerLayout) view.findViewById(R.id.container);
        container.setSwipeEnabled(false);
        container.setNavigationController(this);
        this.container = container;
    }
}
