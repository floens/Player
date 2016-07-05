package org.floens.player.ui.view;

import android.graphics.drawable.Drawable;

import static org.floens.controller.utils.AndroidUtils.getString;

public class BottomBarItem {
    public Drawable drawable;
    public String text;

    public BottomBarItem() {
    }

    public void setText(int resId) {
        text = getString(resId);
    }
}
