package org.floens.player.ui.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import static org.floens.controller.utils.AndroidUtils.dp;

public class Slider extends View {
    private float position;

    private Rect pastRect = new Rect();
    private Paint pastPaint = new Paint();

    private Rect backgroundRect = new Rect();
    private Paint backgroundPaint = new Paint();

    private Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int drawY = dp(12);
    private final int barHeight = dp(2);
    private final int thumbUpRadius = dp(6);
    private final int thumbDownRadius = dp(8);
    private final int thumbGrabRadius = dp(30);
    private final int requiredMinimumChangedPixels = dp(1);

    private int backgroundColor = 0xffFFA893;
    private int pastColor = 0xffFF6A4F;
    private int thumbColor = 0xffFF421C;

    private float thumbRadius = thumbUpRadius;
    private ValueAnimator thumbAnimator;
    private boolean down = false;
    private float downXoffset;
    private ValueAnimator positionAnimator;
    private float drawPosition;

    private OnSliderChanged callback;

    public Slider(Context context) {
        this(context, null);
    }

    public Slider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Slider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnSliderChanged(OnSliderChanged callback) {
        this.callback = callback;
    }

    public void setPosition(float position) {
        setPosition(position, true);
    }

    public void setPosition(float position, boolean animate) {
        setThumbPosition(position, animate, requiredMinimumChangedPixels);
    }

    public void setThumbColor(int thumbColor) {
        this.thumbColor = thumbColor;
    }

    public void setPastColor(int pastColor) {
        this.pastColor = pastColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (down) {
                    break;
                }

                setDown(true);
                if (onThumb(event.getX(), event.getY())) {
                    downXoffset = event.getX() - getThumbX();
                } else {
                    downXoffset = 0f;
                    float pos = (event.getX() + getThumbOffsetX()) /
                            (float) (getWidth() - getPaddingLeft() - getPaddingRight());
                    setThumbPosition(pos, false);
                }

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!down) {
                    break;
                }

                setThumbPosition((event.getX() + getThumbOffsetX() - downXoffset) /
                        (float) (getWidth() - getPaddingLeft() - getPaddingRight()), false);
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                setDown(false);
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    private boolean onThumb(float x, float y) {
        int thumbX = getThumbX();
        return x >= thumbX - thumbGrabRadius && x <= thumbX + thumbGrabRadius;
    }

    private void setDown(boolean down) {
        if (this.down != down) {
            this.down = down;

            if (thumbAnimator != null) {
                thumbAnimator.cancel();
            }
            thumbAnimator = ValueAnimator.ofFloat(thumbRadius, down ? thumbDownRadius : thumbUpRadius);
            thumbAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    thumbRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            thumbAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
            thumbAnimator.setDuration(300);
            thumbAnimator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        int posX = (int) (width * drawPosition);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
//        int barY = (int) (height / 2f - barHeight / 2f);
        int barY = drawY;

        pastRect.set(0, barY, posX, barY + barHeight);
        pastPaint.setColor(pastColor);
        canvas.drawRect(pastRect, pastPaint);

        backgroundRect.set(posX, barY, width, barY + barHeight);
        backgroundPaint.setColor(backgroundColor);
        canvas.drawRect(backgroundRect, backgroundPaint);

        thumbPaint.setColor(thumbColor);
        canvas.drawCircle(posX, barY, thumbRadius, thumbPaint);
        canvas.restore();
    }

    private int getThumbX() {
        return getThumbX(position);
    }

    private int getThumbX(float position) {
        return (int) ((getWidth() - getPaddingLeft() - getPaddingRight()) * position) + getPaddingLeft();
    }

    private int getThumbOffsetX() {
        return -getPaddingLeft();
    }

    private void setThumbPosition(float position, boolean animate) {
        setThumbPosition(position, animate, 1);
    }

    private void setThumbPosition(float position, boolean animate, int minimumChanged) {
        position = Math.min(1f, Math.max(0f, position));
        int thumbDiff = Math.abs(getThumbX() - getThumbX(position));
        if (minimumChanged > 0 && thumbDiff >= minimumChanged) {
            this.position = position;
            if (callback != null) {
                callback.onSliderChanged(position);
            }

            if (animate) {
                if (positionAnimator != null) {
                    positionAnimator.cancel();
                }
                positionAnimator = ValueAnimator.ofFloat(drawPosition, position);
                positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        drawPosition = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                positionAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
                positionAnimator.setDuration(300);
                positionAnimator.start();
            } else {
                drawPosition = position;
            }

            invalidate();
        }
    }

    public interface OnSliderChanged {
        void onSliderChanged(float position);
    }
}
