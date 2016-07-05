package org.floens.player.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.floens.player.R;
import org.floens.player.ui.view.ReactiveButton;

public class PlayerControlsButtons extends LinearLayout {
    private ReactiveButton playButton;
    private ReactiveButton prevButton;
    private ReactiveButton nextButton;

    public PlayerControlsButtons(Context context) {
        this(context, null);
    }

    public PlayerControlsButtons(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControlsButtons(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        playButton = (ReactiveButton) findViewById(R.id.button_play_pause);
        playButton.addDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_56dp));
        playButton.addDrawable(getResources().getDrawable(R.drawable.ic_pause_white_56dp));

        prevButton = (ReactiveButton) findViewById(R.id.button_prev);
        prevButton.addDrawable(getResources().getDrawable(R.drawable.ic_fast_rewind_white_36dp));

        nextButton = (ReactiveButton) findViewById(R.id.button_next);
        nextButton.addDrawable(getResources().getDrawable(R.drawable.ic_fast_forward_white_36dp));
    }
}
