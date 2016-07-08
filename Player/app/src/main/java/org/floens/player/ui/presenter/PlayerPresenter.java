package org.floens.player.ui.presenter;

import org.floens.mpv.MpvCore;
import org.floens.mpv.MpvFormat;
import org.floens.mpv.MpvProperty;
import org.floens.mpv.PropertyObserver;
import org.floens.mpv.renderer.MpvRenderer;
import org.floens.player.PlayerApplication;
import org.floens.player.core.model.FileItem;

import java.util.Locale;

public class PlayerPresenter implements MpvRenderer.Callback, PropertyObserver {
    private PlayerPresenterCallback callback;
    private FileItem fileItem;

    private MpvCore mpvCore;
    private MpvRenderer mpvRenderer;

    private long propertyPause;
    private long propertyTimePosition;
    private long propertyDuration;

    private double currentTime;
    private double totalDuration;

    public PlayerPresenter(PlayerPresenterCallback callback, FileItem fileItem) {
        this.callback = callback;
        this.fileItem = fileItem;

        mpvCore = PlayerApplication.getInstance().getMpvCore();
        mpvRenderer = new MpvRenderer(mpvCore, this);
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
        propertyPause = mpvCore.observeProperty(this, "pause", MpvFormat.FORMAT_FLAG);
        propertyTimePosition = mpvCore.observeProperty(this, "time-pos", MpvFormat.FORMAT_DOUBLE);
        propertyDuration = mpvCore.observeProperty(this, "duration", MpvFormat.FORMAT_DOUBLE);

        mpvCore.command(new String[]{
                "loadfile", fileItem.file.getAbsolutePath()
        });
        requestPlaying(true);
    }

    @Override
    public void mpvRendererUnbound() {
        mpvCore.unobserveProperty(propertyTimePosition);
        mpvCore.unobserveProperty(propertyPause);
        mpvCore.unobserveProperty(propertyDuration);
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
                callback.setControlsTime(formatTime((long) timePosition));
                double position = timePosition / totalDuration;
                callback.setControlsProgress(position);
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
        mpvCore.command(new String[]{
                "set", "pause", playing ? "no" : "yes"
        });
    }

    public interface PlayerPresenterCallback {
        void setControlsPlaying(boolean playing);

        void setControlsProgress(double progress);

        void setControlsTime(String time);

        void setControlsDuration(String duration);
    }
}
