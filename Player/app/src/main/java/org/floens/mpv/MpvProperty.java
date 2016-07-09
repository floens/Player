package org.floens.mpv;

public class MpvProperty {
    public final long userdata;
    public final String name;
    public final MpvNode value;

    public MpvProperty(long userdata, String name, MpvNode value) {
        this.userdata = userdata;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MpvProperty{" +
                "userdata=" + userdata +
                ", name='" + name + '\'' +
                ", format=" + value +
                '}';
    }


}
