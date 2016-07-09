package org.floens.player.core.model;

import java.util.List;

public class Playable {
    public final List<Track> videoTracks;
    public final List<Track> audioTracks;
    public final List<Track> subTracks;

    public Playable(List<Track> videoTracks, List<Track> audioTracks, List<Track> subTracks) {
        this.videoTracks = videoTracks;
        this.audioTracks = audioTracks;
        this.subTracks = subTracks;
    }
}
