package org.floens.player.ui.presenter;

import org.floens.mpv.EventObserver;
import org.floens.mpv.MpvCore;
import org.floens.mpv.MpvNode;
import org.floens.mpv.MpvProperty;
import org.floens.mpv.PropertyObserver;
import org.floens.mpv.renderer.MpvRenderer;
import org.floens.player.PlayerApplication;
import org.floens.player.core.model.FileItem;

import java.util.Locale;

public class PlayerPresenter implements MpvRenderer.Callback, PropertyObserver, EventObserver {
    private static final String TAG = "PlayerPresenter";

    private PlayerPresenterCallback callback;
    private FileItem fileItem;

    private MpvCore mpvCore;
    private MpvRenderer mpvRenderer;

    private long propertyPause;
    private long propertyTimePosition;
    private long propertyDuration;
    private long eventFileLoaded;

    private double currentTime;
    private double totalDuration;

    private long currentControlsTime;

    public PlayerPresenter(PlayerPresenterCallback callback, FileItem fileItem) {
        this.callback = callback;
        this.fileItem = fileItem;

        mpvCore = PlayerApplication.getInstance().getMpvCore();
        mpvRenderer = new MpvRenderer(mpvCore, this);

        callback.setControlsTime(formatTime(0));
        callback.setControlsDuration(formatTime(0));
        callback.setControlsProgress(0f);
    }

    public MpvRenderer getMpvRenderer() {
        return mpvRenderer;
    }

    public void onFocusLost() {
        requestPlaying(false);
    }

    public void onPlayClicked() {
        requestPlaying(true);
    }

    public void onPauseClicked() {
        requestPlaying(false);
    }

    public void onPrevClicked() {
    }

    public void onNextClicked() {
    }

    @Override
    public void mpvRendererBound() {
        propertyPause = mpvCore.observeProperty(this, "pause");
        propertyTimePosition = mpvCore.observeProperty(this, "time-pos");
        propertyDuration = mpvCore.observeProperty(this, "duration");

        mpvCore.command(new String[]{
                "loadfile", fileItem.file.getAbsolutePath()
        });

        eventFileLoaded = mpvCore.observeEvent("file-loaded", this);

        requestPlaying(true);
    }

    @Override
    public void mpvRendererUnbound() {
        mpvCore.unobserveProperty(propertyTimePosition);
        mpvCore.unobserveProperty(propertyPause);
        mpvCore.unobserveProperty(propertyDuration);
        mpvCore.unobserveEvent(eventFileLoaded);
    }

    @Override
    public void onEvent(String name) {
        switch (name) {
            case "file-loaded": {
                break;
            }
        }
    }

    @Override
    public void propertyChanged(MpvProperty property) {
        switch (property.name) {
            case "pause": {
                callback.setControlsPlaying(!property.asBoolean());
                break;
            }
            case "time-pos": {
                double timePosition = property.asDouble();

                if ((long) timePosition != currentControlsTime) {
                    currentControlsTime = (long) timePosition;
                    callback.setControlsTime(formatTime((long) timePosition));

                    double position = timePosition / totalDuration;
                    callback.setControlsProgress(position);
                }

                break;
            }
            case "duration": {
                totalDuration = property.asDouble();
                callback.setControlsDuration(formatTime((long) totalDuration));
                break;
            }
        }
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        seconds -= hours * 3600;

        long minutes = seconds / 60;
        seconds -= minutes * 60;

        if (hours > 0) {
            return String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
        }
    }

    private void requestPlaying(boolean playing) {
        mpvCore.setProperty("pause", new MpvNode(MpvNode.FORMAT_FLAG, !playing));
    }

    public interface PlayerPresenterCallback {
        void setControlsPlaying(boolean playing);

        void setControlsProgress(double progress);

        void setControlsTime(String time);

        void setControlsDuration(String duration);
    }
}
