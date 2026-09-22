package com.jxmann.chunkbound;

public enum ScaleMode {
    CLASSIC("Classic"),
    OLD("Old"),
    CUSTOM("Custom");

    private final String label;

    ScaleMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public ScaleMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
