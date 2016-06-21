package org.floens.controller.ui.layout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TouchBlockingLinearLayout extends LinearLayout {
    public TouchBlockingLinearLayout(Context context) {
        super(context);
    }

    public TouchBlockingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchBlockingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }
}
