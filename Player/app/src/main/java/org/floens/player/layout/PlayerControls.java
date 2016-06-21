package org.floens.player.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.player.R;
import org.floens.player.view.ReactiveButton;
import org.floens.player.view.Seeker;

import static org.floens.controller.AndroidUtils.dp;

public class PlayerControls extends LinearLayout implements ReactiveButton.Callback {
    private static final int MAX_WIDTH = 400;

    private float borderRadius = dp(25);
    private float borderWidth;
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF fillRect = new RectF();
    private Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private TextView title;
    private Seeker seeker;
    private ReactiveButton prevButton;
    private ReactiveButton nextButton;
    private ReactiveButton playPauseButton;

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
        fillPaint.setColor(0x22ffffff);
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setStrokeWidth(borderWidth);
        fillPaint.setPathEffect(new CornerPathEffect(borderRadius));
        int p = (int) (borderWidth) + 1;
        setPadding(p, p, p, p);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
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
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(fillRect, fillPaint);
        canvas.drawRect(fillRect, borderPaint);

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
