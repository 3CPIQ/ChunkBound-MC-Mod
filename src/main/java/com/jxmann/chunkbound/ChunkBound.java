package com.jxmann.chunkbound;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(ChunkBound.MOD_ID)
public final class ChunkBound {
    public static final String MOD_ID = "chunkbound";

    public ChunkBound() {
        MinecraftForge.EVENT_BUS.register(WorldEvents.class);
    }
}
