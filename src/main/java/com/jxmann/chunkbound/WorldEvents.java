package com.jxmann.chunkbound;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        ScaleSelection selection = SELECTIONS.get(server);

        if (level.dimension() == Level.OVERWORLD) {
            selection = ScaleSavedData.get(level).selection();
            SELECTIONS.put(server, selection);
        }

        if (selection != null && selection.finite() && applies(level, selection)) {
            configureBorder(level, selection);
        }
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

        double minX = minChunk(selection.effectiveWidth()) * 16.0;
        double maxX = (minChunk(selection.effectiveWidth()) + selection.effectiveWidth()) * 16.0;
        double minZ = minChunk(selection.effectiveLength()) * 16.0;
        double maxZ = (minChunk(selection.effectiveLength()) + selection.effectiveLength()) * 16.0;
        double x = Math.max(minX + 0.3, Math.min(maxX - 0.3, player.getX()));
        double z = Math.max(minZ + 0.3, Math.min(maxZ - 0.3, player.getZ()));

        if (x != player.getX() || z != player.getZ()) {
            player.teleportTo(level, x, player.getY(), z, player.getYRot(), player.getXRot());
            player.setDeltaMovement(0.0, player.getDeltaMovement().y, 0.0);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SELECTIONS.remove(event.getServer());
    }

    private static boolean applies(ServerLevel level, ScaleSelection selection) {
        return level.dimension() == Level.OVERWORLD || selection.otherDimensions();
    }

    private static void configureBorder(ServerLevel level, ScaleSelection selection) {
        WorldBorder border = level.getWorldBorder();
        double centerX = (minChunk(selection.effectiveWidth()) * 2.0 + selection.effectiveWidth()) * 8.0;
        double centerZ = (minChunk(selection.effectiveLength()) * 2.0 + selection.effectiveLength()) * 8.0;

        border.setCenter(centerX, centerZ);
        border.setSize(Math.max(selection.effectiveWidth(), selection.effectiveLength()) * 16.0);
        border.setWarningBlocks(0);
        border.setDamagePerBlock(1000.0);
        border.setDamageSafeZone(0.0);
    }

    private static int minChunk(int size) {
        return -Math.floorDiv(size, 2);
    }
}
