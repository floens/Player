package org.floens.player.ui.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.floens.controller.Controller;
import org.floens.controller.transition.FadeOutTransition;
import org.floens.controller.ui.drawable.ArrowMenuDrawable;
import org.floens.controller.utils.AndroidUtils;
import org.floens.controller.utils.InsetsHelper;
import org.floens.mpv.egl.EGLView;
import org.floens.player.R;
import org.floens.player.core.model.FileItem;
import org.floens.player.core.model.Track;
import org.floens.player.ui.layout.PlayerControllerContainer;
import org.floens.player.ui.layout.PlayerControls;
import org.floens.player.ui.layout.Slider;
import org.floens.player.ui.presenter.PlayerPresenter;
import org.floens.player.ui.view.Seeker;

import java.util.List;

import static org.floens.controller.utils.AndroidUtils.setRoundItemBackground;

public class PlayerController extends Controller implements View.OnClickListener, PlayerControls.Callback, View.OnSystemUiVisibilityChangeListener, View.OnTouchListener, PlayerControllerContainer.Callback, PlayerPresenter.PlayerPresenterCallback, Slider.OnSliderChanged {
    private static final String TAG = "PlayerController";

    private static final long HIDE_TIME = 1600;

    private PlayerControllerContainer container;
    private EGLView playerSurface;
    private ImageView back;
    private ViewGroup controlsContainer;
    private PlayerControls playerControls;
    private Seeker seeker;

    private Handler handler;
    private GestureDetector gestureDetector;

    private Runnable hideUiRunnable = new Runnable() {
        @Override
        public void run() {
            setUiHidden(true);
        }
    };

    private FileItem fileItem;
    private boolean playing = false;
    private boolean touchDown = false;
    private boolean ignoreSingleTap = false;

    private PlayerPresenter presenter;

    public PlayerController(Context context) {
        super(context);
    }

    public void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        view = inflateRes(R.layout.controller_player);

        container = (PlayerControllerContainer) view;
        container.setCallback(this);

        controlsContainer = (ViewGroup) view.findViewById(R.id.controls_container);
        InsetsHelper.attachInsetsMargin(controlsContainer, true, true, true, true);

        playerSurface = (EGLView) view.findViewById(R.id.player_surface);
        if (fileItem == null) {
            AndroidUtils.removeFromParentView(playerSurface);
        }

        back = (ImageView) view.findViewById(R.id.back);
        ArrowMenuDrawable drawable = new ArrowMenuDrawable();
        drawable.setProgress(1f);
        back.setOnClickListener(this);
        back.setImageDrawable(drawable);
        setRoundItemBackground(back);

        playerControls = (PlayerControls) view.findViewById(R.id.player_controls);
        playerControls.setCallback(this);
        seeker = playerControls.getSeeker();
        seeker.setOnSliderChanged(this);

        view.setOnSystemUiVisibilityChangeListener(this);
        view.setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return PlayerController.this.onSingleTapUp(e);
            }
        });

        handler = new Handler();

        if (fileItem != null) {
            presenter = new PlayerPresenter(this, fileItem);
            playerSurface.setRenderer(presenter.getMpvRenderer());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onShow() {
        super.onShow();
        view.requestApplyInsets();
    }

    @Override
    public void setControlsPlaying(boolean playing) {
        if (this.playing != playing) {
            this.playing = playing;
            playerControls.setPlaying(playing);
            rescheduleHide();
            if (isNavigationHidden() && !playing) {
                setUiHidden(false);
            }
        }
    }

    @Override
    public void setControlsProgress(double progress) {
        seeker.setPosition((float) progress, false);
    }

    @Override
    public void setControlsTime(String time) {
        seeker.setLeftText(time);
    }

    @Override
    public void setControlsDuration(String duration) {
        seeker.setRightText(duration);
    }

    @Override
    public void setControlsTitle(String title) {
        playerControls.setTitle(title);
    }

    @Override
    public void setSubtitleTracks(List<Track> tracks) {
        playerControls.setSubtitleTracks(tracks);
    }

    @Override
    public void setActiveSubtitle(int id) {
        playerControls.setSubtitleSelectedId(id);
    }

    @Override
    public void onSliderChanged(float position) {
        presenter.onSeek(position);
    }

    @Override
    public void onSubtitleClicked(int id) {
        presenter.onSubtitlesClicked(id);
    }

    @Override
    public void prevButtonClicked() {
        presenter.onPrevClicked();
    }

    @Override
    public void nextButtonClicked() {
        presenter.onNextClicked();
    }

    @Override
    public void playButtonClicked() {
        presenter.onPlayClicked();
    }

    @Override
    public void pauseButtonClicked() {
        presenter.onPauseClicked();
    }

    @Override
    public boolean onBack() {
        presenter.onFocusLost();
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
        if (onTouch(event)) {
            gestureDetector.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEventCalled(MotionEvent event) {
        onTouch(event);
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rescheduleHide();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            presenter.onFocusLost();
        }
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        rescheduleHide();
        playerControls.setHidden(isNavigationHidden(), true);
    }

    private boolean onTouch(MotionEvent event) {
        boolean res = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown = true;
                if (isNavigationHidden()) {
                    setUiHidden(false);
                    // Don't give this touch to the gesture detector,
                    // or it'll recognize as a tap and immediately hide the ui again.
                    res = false;
                    ignoreSingleTap = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (ignoreSingleTap) {
                    res = false;
                    ignoreSingleTap = false;
                }
                touchDown = false;
                break;
        }
        rescheduleHide();
        return res;
    }

    private boolean onSingleTapUp(MotionEvent e) {
        setUiHidden(!isNavigationHidden());

        return true;
    }

    private void rescheduleHide() {
        handler.removeCallbacks(hideUiRunnable);
        if (!isNavigationHidden() && playing && !touchDown) {
            handler.postDelayed(hideUiRunnable, HIDE_TIME);
        }
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

    private boolean isNavigationHidden() {
        return (view.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;
    }
}
