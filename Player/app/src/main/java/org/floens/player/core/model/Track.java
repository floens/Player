package org.floens.player.core.model;

public class Track {
    public enum TrackType {
        VIDEO, AUDIO, SUB
    }

    public TrackType trackType;
    public int id;
    public String lang;

    public Track(TrackType trackType, int id, String lang) {
        this.trackType = trackType;
        this.id = id;
        this.lang = lang;
    }
}
