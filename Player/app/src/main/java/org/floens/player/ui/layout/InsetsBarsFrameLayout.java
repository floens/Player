package org.floens.player.ui.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

public class InsetsBarsFrameLayout extends FrameLayout {
    private Rect insets = new Rect();
    private Rect tmpRect = new Rect();

    private boolean drawLeft;
    private boolean drawTop;
    private boolean drawRight;
    private boolean drawBottom;

    private ColorDrawable drawable = new ColorDrawable();

    public InsetsBarsFrameLayout(Context context) {
        this(context, null);
    }

    public InsetsBarsFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InsetsBarsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setColor(0xff000000);
        setWillNotDraw(true);
        requestApplyInsets();
    }

    public void setColor(int color) {
        drawable.setColor(color);
    }

    public void setDrawBars(boolean left, boolean top, boolean right, boolean bottom) {
        drawLeft = left;
        drawTop = top;
        drawRight = right;
        drawBottom = bottom;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        insets.set(windowInsets.getSystemWindowInsetLeft(),
                windowInsets.getSystemWindowInsetTop(),
                windowInsets.getSystemWindowInsetRight(),
                windowInsets.getSystemWindowInsetBottom());

        setPadding(
                drawLeft ? insets.left : 0,
                drawTop ? insets.top : 0,
                drawRight ? insets.right : 0,
                drawBottom ? insets.bottom : 0
        );

        setWillNotDraw(insets.left == 0 && insets.top == 0 && insets.right == 0 && insets.bottom == 0);
//        ViewCompat.postInvalidateOnAnimation(ScrimInsetsFrameLayout.this);
        invalidate();
        return windowInsets;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        int sc = canvas.save();
        canvas.translate(getScrollX(), getScrollY());

        if (drawTop) {
            tmpRect.set(0, 0, width, insets.top);
            drawBounds(canvas);
        }

        if (drawBottom) {
            tmpRect.set(0, height - insets.bottom, width, height);
            drawBounds(canvas);
        }

        if (drawLeft) {
            tmpRect.set(0, 0, insets.left, height);
            drawBounds(canvas);
        }

        if (drawRight) {
            tmpRect.set(width - insets.right, 0, width, height);
            drawBounds(canvas);
        }

        canvas.restoreToCount(sc);
    }

    private void drawBounds(@NonNull Canvas canvas) {
        if (!tmpRect.isEmpty()) {
            drawable.setBounds(tmpRect);
            drawable.draw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }
}
