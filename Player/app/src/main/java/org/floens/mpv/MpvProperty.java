package org.floens.mpv;

public class MpvProperty {
    public final long userdata;
    public final String name;
    public final MpvFormat format;

    public MpvProperty(long userdata, String name, MpvFormat format) {
        this.userdata = userdata;
        this.name = name;
        this.format = format;
    }

    @Override
    public String toString() {
        return "MpvProperty{" +
                "userdata=" + userdata +
                ", name='" + name + '\'' +
                ", format=" + format +
                '}';
    }

    public boolean asBoolean() {
        return (boolean) format.value;
    }

    public double asDouble() {
        return format.value == null ? 0.0 : (double) format.value;
    }
}
