package org.floens.player.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class FitRecyclerView extends RecyclerView {
    public FitRecyclerView(Context context) {
        this(context, null);
    }

    public FitRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        setPadding(
                insets.left + getPaddingLeft(),
                insets.top + getPaddingTop(),
                insets.right + getPaddingRight(),
                insets.bottom + getPaddingBottom()
        );

        return true;
    }
}
