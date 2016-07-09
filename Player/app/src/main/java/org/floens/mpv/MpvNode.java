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

    @SuppressWarnings("UnnecessaryBoxing")
    public MpvNode(int format, Object value) {
        this.format = format;
        this.value = value;
        // Automatically convert boxed integers to longs
        if (this.value instanceof Integer) {
            this.value = Long.valueOf((int) value);
        }
    }

    @Override
    public String toString() {
        return "MpvNode{" +
                "format=" + format +
                ", value=" + value +
                '}';
    }

    public boolean asBoolean() {
        return (boolean) value;
    }

    public int asInt() {
        return (int) asLong();
    }

    public long asLong() {
        return (long) value;
    }

    public double asDouble() {
        return value == null ? 0.0 : (double) value;
    }

    public String asString() {
        return (String) value;
    }
}
