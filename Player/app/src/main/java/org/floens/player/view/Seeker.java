package org.floens.player.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import org.floens.player.layout.Slider;

import static org.floens.controller.AndroidUtils.dp;
import static org.floens.controller.AndroidUtils.sp;

public class Seeker extends Slider {
    private Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int timeTextSize = sp(12);
    private final int timeWidth = dp(42 - 12);

    public Seeker(Context context) {
        this(context, null);
    }

    public Seeker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Seeker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        timePaint.setColor(0xffffffff);
        timePaint.setTextSize(timeTextSize);

        setThumbColor(0xffffffff);
        setBackgroundColor(0x11ffffff);
        setPastColor(0x44ffffff);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int left = timeWidth;
        int right = getWidth() - timeWidth;
        int y = getHeight() / 2 + timeTextSize / 2;

        drawTime(canvas, "1:49", left, y, true);
        drawTime(canvas, "2:34", right, y, false);
    }

    private void drawTime(Canvas canvas, String text, int x, int y, boolean floatRight) {
        if (floatRight) {
            x -= timePaint.measureText(text);
        }
        canvas.drawText(text, x, y, timePaint);
    }
}
