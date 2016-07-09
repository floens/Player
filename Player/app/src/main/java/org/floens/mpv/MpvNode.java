package org.floens.mpv;

public class MpvNode {
    public static final int FORMAT_NONE = 0;
    public static final int FORMAT_STRING = 1;
    public static final int FORMAT_OSD_STRING = 2;
    public static final int FORMAT_FLAG = 3;
    public static final int FORMAT_INT64 = 4;
    public static final int FORMAT_DOUBLE = 5;

    public int format;
    public Object value;

    public MpvNode(int format, Object value) {
        this.format = format;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MpvNode{" +
                "format=" + format +
                ", value=" + value +
                '}';
    }
}
