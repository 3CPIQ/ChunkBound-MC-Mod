package com.jxmann.chunkbound;

public record ScaleSelection(ScaleMode mode, int width, int length, boolean otherDimensions) {
    public static ScaleSelection classic() {
        return new ScaleSelection(ScaleMode.CLASSIC, 16, 16, false);
    }

    public int effectiveWidth() {
        return mode == ScaleMode.OLD ? 16 : width;
    }

    public int effectiveLength() {
        return mode == ScaleMode.OLD ? 16 : length;
    }

    public boolean finite() {
        return mode != ScaleMode.CLASSIC;
    }
}
