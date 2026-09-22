package com.jxmann.chunkbound;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

public final class WorldEvents {
    private static final Map<MinecraftServer, ScaleSelection> SELECTIONS = new WeakHashMap<>();

    private WorldEvents() {
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        MinecraftServer server = level.getServer();

        if (level.dimension() == Level.OVERWORLD) {
            SELECTIONS.put(server, ScaleSavedData.get(level).selection());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        ScaleSelection selection = SELECTIONS.get(level.getServer());

        if (selection == null || !selection.finite() || !applies(level, selection) || contains(chunk.getPos(), selection)) {
            return;
        }

        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        LevelChunkSection[] sections = chunk.getSections();

        for (int i = 0; i < sections.length; i++) {
            sections[i] = new LevelChunkSection(biomes);
        }

        chunk.clearAllBlockEntities();
        chunk.setAllStarts(Map.of());
        chunk.setAllReferences(Map.of());
        Heightmap.primeHeightmaps(chunk, EnumSet.allOf(Heightmap.Types.class));
        chunk.setLightCorrect(false);
        chunk.initializeLightSources();
        chunk.setUnsaved(true);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ScaleSelection selection = SELECTIONS.get(server);

        if (selection == null || !selection.finite()) {
            return;
        }

        ServerLevel level = server.overworld();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        y = Math.max(y + 1, level.getMinBuildHeight() + 2);
        level.setDefaultSpawnPos(new BlockPos(0, y, 0), 0.0F);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        ScaleSelection selection = SELECTIONS.get(level.getServer());

        if (selection == null || !selection.finite() || !applies(level, selection)) {
            return;
        }

        if (contains(player.chunkPosition(), selection) || player.getY() > level.getMinBuildHeight() - 8) {
            return;
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        player.teleportTo(level, 0.5, Math.max(y + 1, level.getMinBuildHeight() + 2), 0.5, player.getYRot(), player.getXRot());
        player.setDeltaMovement(0.0, 0.0, 0.0);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SELECTIONS.remove(event.getServer());
    }

    public static boolean shouldBeVoid(ServerLevel level, ChunkPos pos) {
        ScaleSelection selection = SELECTIONS.get(level.getServer());
        return selection != null && selection.finite() && applies(level, selection) && !contains(pos, selection);
    }

    private static boolean applies(ServerLevel level, ScaleSelection selection) {
        return level.dimension() == Level.OVERWORLD || selection.otherDimensions();
    }

    private static boolean contains(ChunkPos pos, ScaleSelection selection) {
        int minX = minChunk(selection.effectiveWidth());
        int minZ = minChunk(selection.effectiveLength());

        return pos.x >= minX && pos.x < minX + selection.effectiveWidth()
            && pos.z >= minZ && pos.z < minZ + selection.effectiveLength();
    }

    private static int minChunk(int size) {
        return -Math.floorDiv(size, 2);
    }
}
