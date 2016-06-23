package org.floens.player.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.player.R;
import org.floens.player.view.ReactiveButton;
import org.floens.player.view.Seeker;

import static org.floens.controller.AndroidUtils.dp;

public class PlayerControls extends LinearLayout implements ReactiveButton.Callback {
    private static final int MAX_WIDTH = 500;

    private float borderRadius = dp(25);
    private float borderWidth;
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int borderAlpha = 0xaa;
    private RectF fillRect = new RectF();
    private Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int fillAlpha = 0xdd;

    private TextView title;
    private Seeker seeker;
    private ReactiveButton prevButton;
    private ReactiveButton nextButton;
    private ReactiveButton playPauseButton;

    private boolean hidden = false;
    private ValueAnimator alphaAnimator;
    private float drawnAlpha = 1f;

    private View back;

    private Callback callback;

    public PlayerControls(Context context) {
        this(context, null);
    }

    public PlayerControls(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControls(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);

        borderWidth = dp(3);
        borderPaint.setColor(0xaaffffff);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setPathEffect(new CornerPathEffect(borderRadius));
        fillPaint.setColor(0xdd000000);
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setStrokeWidth(borderWidth);
        fillPaint.setPathEffect(new CornerPathEffect(borderRadius));
        int p = (int) (borderWidth) + 1;
        setPadding(p, p, p, p);
    }

    public void setHidden(boolean hidden, boolean animate) {
        if (this.hidden != hidden) {
            this.hidden = hidden;

            float targetAlpha = hidden ? 0f : 1f;
            if (animate) {
                if (alphaAnimator != null) {
                    alphaAnimator.cancel();
                }
                alphaAnimator = ValueAnimator.ofFloat(drawnAlpha, targetAlpha);
                alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        drawnAlpha = (float) animation.getAnimatedValue();
                        setAlpha(drawnAlpha);
                        invalidate();
                    }
                });

                if (hidden) {
                    alphaAnimator.setStartDelay(100);
                    alphaAnimator.setDuration(250);
                    alphaAnimator.setInterpolator(new AccelerateInterpolator(1.5f));
                } else {
                    alphaAnimator.setDuration(250);
                    alphaAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
                }
                alphaAnimator.start();
            } else {
                setAlpha(targetAlpha);
                drawnAlpha = targetAlpha;
            }
            invalidate();
        }
    }

    public void setPlaying(boolean playing) {
        playPauseButton.setSelected(playing ? 1 : 0);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        setVisibility(alpha == 0f ? GONE : VISIBLE);

        if (back != null) {
            back.setAlpha(alpha);
            back.setVisibility(alpha == 0f ? GONE : VISIBLE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        title = (TextView) findViewById(R.id.title);
        seeker = (Seeker) findViewById(R.id.seeker);
        prevButton = (ReactiveButton) findViewById(R.id.button_prev);
        prevButton.setCallback(this);
        nextButton = (ReactiveButton) findViewById(R.id.button_next);
        nextButton.setCallback(this);
        playPauseButton = (ReactiveButton) findViewById(R.id.button_play_pause);
        playPauseButton.setCallback(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        back = ((ViewGroup) getParent()).findViewById(R.id.back);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        back = null;
    }

    @Override
    public void onButtonSelectedChanged(ReactiveButton button, int selected) {
        if (button == prevButton) {
            callback.prevButtonClicked();
        } else if (button == nextButton) {
            callback.nextButtonClicked();
        } else if (button == playPauseButton) {
            if (selected == 0) {
                callback.pauseButtonClicked();
            } else {
                callback.playButtonClicked();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (drawnAlpha > 0f) {
//            fillPaint.setAlpha((int) (drawnAlpha * fillAlpha));
//            borderPaint.setAlpha((int) (drawnAlpha * borderAlpha));
        canvas.drawRect(fillRect, fillPaint);
        canvas.drawRect(fillRect, borderPaint);
//        }

        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int halfWidth = (int) (borderWidth / 2f);
            fillRect.set(
                    getPaddingLeft() - halfWidth,
                    getPaddingTop() - halfWidth,
                    getWidth() - getPaddingRight() + halfWidth,
                    getHeight() - getPaddingBottom() + halfWidth
            );
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthMode = MeasureSpec.EXACTLY;
            widthSize = dp(MAX_WIDTH);
        } else {
            widthMode = MeasureSpec.EXACTLY;
            widthSize = Math.min(widthSize, dp(MAX_WIDTH));
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), heightMeasureSpec);
    }

    public interface Callback {
        void prevButtonClicked();

        void nextButtonClicked();

        void pauseButtonClicked();

        void playButtonClicked();
    }
}
