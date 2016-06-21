package org.floens.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AlbumView extends ImageView {
    public AlbumView(Context context) {
        this(context, null);
    }

    public AlbumView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
    }
}
