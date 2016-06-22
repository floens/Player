package org.floens.player.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class PlayerControllerContainer extends FrameLayout {
    private Callback callback;

    public PlayerControllerContainer(Context context) {
        this(context, null);
    }

    public PlayerControllerContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControllerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (callback != null) {
            return callback.onInterceptTouchEventCalled(ev);
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public interface Callback {
        boolean onInterceptTouchEventCalled(MotionEvent ev);
    }
}
