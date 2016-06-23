package org.floens.player;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

public class InsetsHelper {
    public static void attachInsetsPadding(
            final View view,
            final boolean left, final boolean top,
            final boolean right, final boolean bottom) {
        final int originalLeft = view.getPaddingLeft();
        final int originalTop = view.getPaddingTop();
        final int originalRight = view.getPaddingRight();
        final int originalBottom = view.getPaddingBottom();

        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int insetsLeft = left ? insets.getSystemWindowInsetLeft() : 0;
                int insetsTop = top ? insets.getSystemWindowInsetTop() : 0;
                int insetsRight = right ? insets.getSystemWindowInsetRight() : 0;
                int insetsBottom = bottom ? insets.getSystemWindowInsetBottom() : 0;

                view.setPadding(
                        originalLeft + insetsLeft,
                        originalTop + insetsTop,
                        originalRight + insetsRight,
                        originalBottom + insetsBottom
                );

                return insets;
            }
        });
    }

    public static void attachInsetsMargin(
            final View view,
            final boolean left, final boolean top,
            final boolean right, final boolean bottom) {
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("Only for views with MarginLayoutParams");
        }

        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int originalLeft = marginParams.leftMargin;
        final int originalTop = marginParams.topMargin;
        final int originalRight = marginParams.rightMargin;
        final int originalBottom = marginParams.bottomMargin;

        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int insetsLeft = left ? insets.getSystemWindowInsetLeft() : 0;
                int insetsTop = top ? insets.getSystemWindowInsetTop() : 0;
                int insetsRight = right ? insets.getSystemWindowInsetRight() : 0;
                int insetsBottom = bottom ? insets.getSystemWindowInsetBottom() : 0;

                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                marginParams.leftMargin = originalLeft + insetsLeft;
                marginParams.topMargin = originalTop + insetsTop;
                marginParams.rightMargin = originalRight + insetsRight;
                marginParams.bottomMargin = originalBottom + insetsBottom;
                view.setLayoutParams(marginParams);

                return insets;
            }
        });
    }
}
