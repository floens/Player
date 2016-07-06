package org.floens.mpv;

public class MpvProperty {
    public final String name;
    public final MpvFormat format;
    public final Object value;

    public MpvProperty(String name, MpvFormat format, Object value) {
        this.name = name;
        this.format = format;
        this.value = value;
    }
}
