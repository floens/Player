package org.floens.player.ui.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.floens.player.R;

import java.util.ArrayList;
import java.util.List;

import static org.floens.controller.utils.AndroidUtils.dp;
import static org.floens.controller.utils.AndroidUtils.getAttrColor;
import static org.floens.controller.utils.AndroidUtils.sp;

public class BottomBar extends View {
    private final int textActiveSize = sp(14);
    private final int textInactiveSize = sp(12);
    private int textInactiveColor;
    private int textActiveColor;
    private final int iconActiveTopPadding = dp(6);
    private final int iconInactiveTopPadding = dp(8);
    private final int textBottomPadding = dp(10);

    private List<BottomBarItemView> itemViews = new ArrayList<>();

    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean down = false;
    private float downX;

    private BottomBarItem activeItem;
    private BottomBarCallback callback;

    public BottomBar(Context context) {
        this(context, null);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        textInactiveColor = getAttrColor(getContext(), R.attr.text_color_secondary);
        textActiveColor = getAttrColor(getContext(), R.attr.colorAccent);
    }

    public void setCallback(BottomBarCallback callback) {
        this.callback = callback;
    }

    public void addItem(BottomBarItem item) {
        itemViews.add(new BottomBarItemView(item));
        if (itemViews.size() == 1) {
            setActive(item, false);
        }
    }

    public void setActive(BottomBarItem activeItem, boolean animated) {
        for (int i = 0; i < itemViews.size(); i++) {
            BottomBarItemView itemView = itemViews.get(i);
            if (itemView.item == activeItem) {
                changeActive(itemView, animated);
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!down) {
                    down = true;
                    downX = event.getX();
                }

                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                down = false;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (down) {
                    down = false;
                    BottomBarItemView downItem = getItemViewForPosition(downX);
                    BottomBarItemView upItem = getItemViewForPosition(event.getX());
                    if (downItem == upItem) {
                        changeActive(upItem, true);
                    }
                }
            }
        }

        return true;
    }

    private BottomBarItemView getItemViewForPosition(float x) {
        float itemWidth = getWidth() / (float) itemViews.size();
        int index = (int) (x / itemWidth);
        if (index >= 0 && index < itemViews.size()) {
            return itemViews.get(index);
        } else {
            return null;
        }
    }

    private void changeActive(BottomBarItemView activeItem, boolean animated) {
        if (this.activeItem != activeItem.item) {
            this.activeItem = activeItem.item;
            for (int i = 0; i < itemViews.size(); i++) {
                BottomBarItemView item = itemViews.get(i);
                if (item != activeItem) {
                    item.setActive(false, animated);
                }
            }
            activeItem.setActive(true, animated);
            invalidate();
            if (callback != null) {
                callback.onBottomBarItemSelected(activeItem.item);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int itemWidth = (int) (getWidth() / (float) itemViews.size());
        for (int i = 0; i < itemViews.size(); i++) {
            BottomBarItemView item = itemViews.get(i);
            canvas.save();
            canvas.translate(itemWidth * i, 0);
            drawItem(canvas, item, itemWidth);
            canvas.restore();
        }
    }

    private void drawItem(Canvas canvas, BottomBarItemView item, int width) {
        float iconX = ((width / 2f) - (item.drawable.getIntrinsicHeight() / 2f));
        float iconY = item.iconY;

        DrawableCompat.setTint(item.drawable, item.color);
        item.drawable.setBounds(0, 0, item.drawable.getIntrinsicWidth(), item.drawable.getIntrinsicHeight());
        canvas.save();
        canvas.translate(iconX, iconY);
        item.drawable.draw(canvas);
        canvas.restore();

        textPaint.setTextSize(item.textSize);
        textPaint.setColor(item.color);

        int textBaselineY = getHeight() - textBottomPadding;
        float scale = item.textSize / textActiveSize;
        if (scale != 1f) {
            canvas.save();
        }
        textPaint.setTextSize(textActiveSize);
        float textWidth = textPaint.measureText(item.text, 0, item.text.length()) * scale;
        float textX = (width / 2f) - (textWidth / 2f);
        if (scale != 1f) {
            canvas.scale(scale, scale, textX, textBaselineY);
        }
        canvas.drawText(item.text, textX, textBaselineY, textPaint);
        if (scale != 1f) {
            canvas.restore();
        }
    }

    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new LogDecelerateInterpolator(
            400f, 1.4f, 0);

    /**
     * Interpolator with a smooth log deceleration.
     */
    private static final class LogDecelerateInterpolator implements TimeInterpolator {
        private final float mBase;
        private final float mDrift;
        private final float mTimeScale;
        private final float mOutputScale;

        public LogDecelerateInterpolator(float base, float timeScale, float drift) {
            mBase = base;
            mDrift = drift;
            mTimeScale = 1f / timeScale;

            mOutputScale = 1f / computeLog(1f);
        }

        private float computeLog(float t) {
            return 1f - (float) Math.pow(mBase, -t * mTimeScale) + (mDrift * t);
        }

        @Override
        public float getInterpolation(float t) {
            return computeLog(t) * mOutputScale;
        }
    }

    private class BottomBarItemView {
        private BottomBarItem item;

        private Drawable drawable;
        private String text;
        private int color = textInactiveColor;
        private float textSize = textInactiveSize;
        private float iconY = iconInactiveTopPadding;
        private boolean active = false;

        private ValueAnimator textSizeAnimator;
        private ValueAnimator iconAnimator;
        private ValueAnimator colorAnimator;

        public BottomBarItemView(BottomBarItem item) {
            this.item = item;
            drawable = DrawableCompat.wrap(item.drawable);
            text = item.text;

            // TODO: Setting the tint just before drawing it doesn't work
            DrawableCompat.setTint(drawable, color);
        }

        private void setActive(boolean active, boolean animate) {
            if (this.active != active) {
                this.active = active;

                if (animate) {
                    if (textSizeAnimator != null) {
                        textSizeAnimator.cancel();
                    }

                    textSizeAnimator = ValueAnimator.ofFloat(textSize, active ? textActiveSize : textInactiveSize);
                    textSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            textSize = (float) animation.getAnimatedValue();
                            invalidate();
                        }
                    });
                    textSizeAnimator.setInterpolator(DECELERATE_INTERPOLATOR);
                    textSizeAnimator.setDuration(300).start();

                    if (iconAnimator != null) {
                        iconAnimator.cancel();
                    }

                    iconAnimator = ValueAnimator.ofFloat(iconY, active ? iconActiveTopPadding : iconInactiveTopPadding);
                    iconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            iconY = (float) animation.getAnimatedValue();
                            invalidate();
                        }
                    });
                    iconAnimator.setInterpolator(DECELERATE_INTERPOLATOR);
                    iconAnimator.setDuration(300).start();

                    if (colorAnimator != null) {
                        colorAnimator.cancel();
                    }

                    colorAnimator = ValueAnimator.ofArgb(color, active ? textActiveColor : textInactiveColor);
                    colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            color = (int) animation.getAnimatedValue();
                            invalidate();
                        }
                    });
                    colorAnimator.setInterpolator(DECELERATE_INTERPOLATOR);
                    colorAnimator.setDuration(300).start();
                } else {
                    if (active) {
                        textSize = textActiveSize;
                        iconY = iconActiveTopPadding;
                        color = textActiveColor;
                    } else {
                        textSize = textInactiveSize;
                        iconY = iconInactiveTopPadding;
                        color = textInactiveColor;
                    }
                    invalidate();
                }
            }
        }
    }

    public interface BottomBarCallback {
        void onBottomBarItemSelected(BottomBarItem item);
    }
}
