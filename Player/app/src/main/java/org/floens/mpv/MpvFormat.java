package org.floens.mpv;

public enum MpvFormat {
    NONE(0),
    STRING(1),
    OSD_STRING(2),
    FLAG(3),
    LONG(4),
    DOUBLE(5);

    public final int nativeInt;

    MpvFormat(int nativeInt) {
        this.nativeInt = nativeInt;
    }
}
