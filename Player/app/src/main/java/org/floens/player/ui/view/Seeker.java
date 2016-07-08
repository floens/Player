package org.floens.player.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.floens.player.ui.layout.Slider;

import static org.floens.controller.utils.AndroidUtils.dp;
import static org.floens.controller.utils.AndroidUtils.sp;

public class Seeker extends Slider {
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int textTextSize = sp(12);
    private final int textWidth = dp(42 - 12);

    private String leftText;
    private String rightText;

    public Seeker(Context context) {
        this(context, null);
    }

    public Seeker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Seeker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        textPaint.setColor(0xffffffff);
        textPaint.setTextSize(textTextSize);

        setThumbColor(0xffffffff);
        setBackgroundColor(0x11ffffff);
        setPastColor(0x44ffffff);
    }

    public void setLeftText(String leftText) {
        if (!TextUtils.equals(this.leftText, leftText)) {
            this.leftText = leftText;
            invalidate();
        }
    }

    public void setRightText(String rightText) {
        if (!TextUtils.equals(this.rightText, rightText)) {
            this.rightText = rightText;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = textWidth;
        int right = getWidth() - textWidth;
        int y = getHeight() / 2 + textTextSize / 2;

        if (leftText != null) {
            drawtext(canvas, leftText, left, y, true);
        }
        if (rightText != null) {
            drawtext(canvas, rightText, right, y, false);
        }
    }

    private void drawtext(Canvas canvas, String text, int x, int y, boolean floatRight) {
        if (floatRight) {
            x -= textPaint.measureText(text);
        }
        canvas.drawText(text, x, y, textPaint);
    }
}
