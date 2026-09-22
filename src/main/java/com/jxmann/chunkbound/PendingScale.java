package com.jxmann.chunkbound;

public final class PendingScale {
    private static ScaleSelection selection = ScaleSelection.classic();

    private PendingScale() {
    }

    public static ScaleSelection get() {
        return selection;
    }

    public static void set(ScaleSelection value) {
        selection = value;
    }

    public static ScaleSelection take() {
        ScaleSelection value = selection;
        selection = ScaleSelection.classic();
        return value;
    }
}
