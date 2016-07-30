package org.floens.player.ui.presenter;

import org.floens.mpv.EventObserver;
import org.floens.mpv.MpvCore;
import org.floens.mpv.MpvNode;
import org.floens.mpv.MpvProperty;
import org.floens.mpv.PropertyObserver;
import org.floens.mpv.renderer.MpvRenderer;
import org.floens.player.PlayerApplication;
import org.floens.player.core.model.FileItem;
import org.floens.player.core.model.Playable;
import org.floens.player.core.model.Track;

import java.util.ArrayList;
import java.util.List;
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
    private long propertyMediaTitle;
    private long propertyTrackList;
    private long propertySub;
    private long propertyHwDec;
    private long eventFileLoaded;

    private Playable playable;
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

    public void onSubtitlesClicked(int id) {
        if (playable != null) {
            mpvCore.setProperty("sub", new MpvNode(MpvNode.FORMAT_INT64, id));
        }
    }

    public void toggleHwDec() {
        MpvNode currentHwDecProperty = mpvCore.getProperty("hwdec");
        boolean isHardwareDecoding = isHardwareDecoding(currentHwDecProperty);
        String value = isHardwareDecoding ? "no" : "mediacodec";
        mpvCore.setProperty("hwdec", new MpvNode(MpvNode.FORMAT_STRING, value));
    }

    public void onSeek(float position) {
        double time = totalDuration * position;
        mpvCore.setProperty("time-pos", new MpvNode(MpvNode.FORMAT_DOUBLE, time));
    }

    @Override
    public void mpvRendererBound() {
        propertyPause = mpvCore.observeProperty(this, "pause");
        propertyTimePosition = mpvCore.observeProperty(this, "time-pos");
        propertyDuration = mpvCore.observeProperty(this, "duration");
        propertyMediaTitle = mpvCore.observeProperty(this, "media-title");
        propertyTrackList = mpvCore.observeProperty(this, "track-list");
        propertySub = mpvCore.observeProperty(this, "sub");
        propertyHwDec = mpvCore.observeProperty(this, "hwdec");

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
        mpvCore.unobserveProperty(propertyMediaTitle);
        mpvCore.unobserveProperty(propertyTrackList);
        mpvCore.unobserveProperty(propertySub);
        mpvCore.unobserveProperty(propertyHwDec);
        mpvCore.unobserveEvent(eventFileLoaded);
    }

    @Override
    public void onEvent(String name) {
        switch (name) {
            case "file-loaded": {
                int trackCount = mpvCore.getProperty("track-list/count").asInt();

                List<Track> videoTracks = new ArrayList<>();
                List<Track> audioTracks = new ArrayList<>();
                List<Track> subTracks = new ArrayList<>();

                for (int i = 0; i < trackCount; i++) {
                    int id = mpvCore.getProperty("track-list/" + i + "/id").asInt();
                    String type = mpvCore.getProperty("track-list/" + i + "/type").asString();
                    MpvNode langProperty = mpvCore.getProperty("track-list/" + i + "/lang");
                    String lang = langProperty != null ? langProperty.asString() : "unknown";

                    switch (type) {
                        case "video":
                            videoTracks.add(new Track(Track.TrackType.VIDEO, id, lang));
                            break;
                        case "audio":
                            audioTracks.add(new Track(Track.TrackType.AUDIO, id, lang));
                            break;
                        case "sub":
                            subTracks.add(new Track(Track.TrackType.SUB, id, lang));
                            break;
                    }
                }

                playable = new Playable(videoTracks, audioTracks, subTracks);

                callback.setSubtitleTracks(subTracks);

                break;
            }
        }
    }

    @Override
    public void propertyChanged(MpvProperty property) {
        switch (property.name) {
            case "track-list": {
                break;
            }
            case "pause": {
                callback.setControlsPlaying(!property.value.asBoolean());
                break;
            }
            case "time-pos": {
                double timePosition = property.value.asDouble();

                if ((long) timePosition != currentControlsTime) {
                    currentControlsTime = (long) timePosition;
                    callback.setControlsTime(formatTime((long) timePosition));

                    double position = timePosition / totalDuration;
                    callback.setControlsProgress(position);
                }

                break;
            }
            case "duration": {
                totalDuration = property.value.asDouble();
                callback.setControlsDuration(formatTime((long) totalDuration));
                break;
            }
            case "media-title": {
                callback.setControlsTitle(property.value.asString());
                break;
            }
            case "sub": {
                // boolean false when disabled, int64 subid when enabled
                int activeId = 0;
                if (property.value.format == MpvNode.FORMAT_INT64) {
                    activeId = property.value.asInt();
                }

                callback.setActiveSubtitle(activeId);
                break;
            }
            case "hwdec": {
                boolean hardwareDecoding = isHardwareDecoding(property.value);
                callback.setHardwareDecodingActive(hardwareDecoding);

                break;
            }
        }
    }

    private boolean isHardwareDecoding(MpvNode property) {
        return "mediacodec".equals(property.value);
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

        void setControlsTitle(String title);

        void setSubtitleTracks(List<Track> tracks);

        void setActiveSubtitle(int id);

        void setHardwareDecodingActive(boolean hardwareDecodingActive);
    }
}
