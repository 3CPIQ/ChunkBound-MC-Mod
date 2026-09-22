package com.jxmann.chunkbound;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class ScaleSavedData extends SavedData {
    private static final String FILE_NAME = "chunkbound_scale";
    private final ScaleSelection selection;

    private ScaleSavedData(ScaleSelection selection) {
        this.selection = selection;
        setDirty();
    }

    public static ScaleSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            ScaleSavedData::load,
            () -> new ScaleSavedData(PendingScale.take()),
            FILE_NAME
        );
    }

    public static ScaleSavedData load(CompoundTag tag) {
        ScaleMode mode;

        try {
            mode = ScaleMode.valueOf(tag.getString("mode"));
        } catch (IllegalArgumentException exception) {
            mode = ScaleMode.CLASSIC;
        }

        int width = Math.max(1, tag.getInt("width"));
        int length = Math.max(1, tag.getInt("length"));
        boolean otherDimensions = tag.getBoolean("otherDimensions");

        return new ScaleSavedData(new ScaleSelection(mode, width, length, otherDimensions));
    }

    public ScaleSelection selection() {
        return selection;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("mode", selection.mode().name());
        tag.putInt("width", selection.width());
        tag.putInt("length", selection.length());
        tag.putBoolean("otherDimensions", selection.otherDimensions());
        return tag;
    }
}
