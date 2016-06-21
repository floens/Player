package org.floens.player.controller;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.floens.controller.Controller;
import org.floens.controller.transition.FadeOutTransition;
import org.floens.controller.ui.drawable.ArrowMenuDrawable;
import org.floens.player.R;
import org.floens.player.layout.PlayerControls;

import static org.floens.controller.AndroidUtils.setRoundItemBackground;

public class PlayerController extends Controller implements View.OnClickListener, PlayerControls.Callback, View.OnSystemUiVisibilityChangeListener, View.OnTouchListener {
    private static final String TAG = "PlayerController";

    private static final long HIDE_TIME = 800;

    private ImageView back;
    private PlayerControls playerControls;

    private boolean playing = false;

    private Handler handler;
    private GestureDetector gestureDetector;

    private Runnable hideUiRunnable = new Runnable() {
        @Override
        public void run() {
            setUiHidden(true);
        }
    };

    public PlayerController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        view = inflateRes(R.layout.controller_player);

        back = (ImageView) view.findViewById(R.id.back);
        ArrowMenuDrawable drawable = new ArrowMenuDrawable();
        drawable.setProgress(1f);
        back.setOnClickListener(this);
        back.setImageDrawable(drawable);
        setRoundItemBackground(back);

        playerControls = (PlayerControls) view.findViewById(R.id.player_controls);
        playerControls.setCallback(this);

        view.setOnSystemUiVisibilityChangeListener(this);
        view.setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return PlayerController.this.onSingleTapUp(e);
            }
        });

        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void prevButtonClicked() {
    }

    @Override
    public void nextButtonClicked() {
    }

    @Override
    public void pauseButtonClicked() {
        playing = false;
        playingChanged();
    }

    @Override
    public void playButtonClicked() {
        playing = true;
        playingChanged();
    }

    @Override
    public boolean onBack() {
        navigationController.popController(new FadeOutTransition());
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            onBack();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private boolean onSingleTapUp(MotionEvent e) {
        setUiHidden(!isNavigationHidden());

        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            setUiHidden(false);

            playing = false;
            playingChanged();
        }
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.i(TAG, "onSystemUiVisibilityChange() called with: " + "visibility = [" + visibility + "]");

        updateHideState();
    }

    private void playingChanged() {
        updateHideState();
    }

    private void updateHideState() {
        handler.removeCallbacks(hideUiRunnable);
        if (!isNavigationHidden() && playing) {
            handler.postDelayed(hideUiRunnable, HIDE_TIME);
        }
    }

    private boolean isNavigationHidden() {
        return (view.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
    }

    private void setUiHidden(boolean uiHidden) {
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (uiHidden) {
            flags |= View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        view.setSystemUiVisibility(flags);
    }
}
