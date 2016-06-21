package org.floens.player.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import org.floens.player.R;
import org.floens.player.view.ReactiveButton;

public class PlayerBar extends LinearLayout {
    private ReactiveButton playButton;

    public PlayerBar(Context context) {
        this(context, null);
    }

    public PlayerBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        playButton = (ReactiveButton) findViewById(R.id.button_play_pause);
        playButton.addDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_36dp));
        playButton.addDrawable(getResources().getDrawable(R.drawable.ic_pause_black_36dp));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    /*@SuppressWarnings("deprecation")
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        setPadding(
                getPaddingLeft(),
                getPaddingTop(),
                getPaddingRight(),
                getPaddingBottom() + insets.bottom
        );
        return false;
    }*/
}
