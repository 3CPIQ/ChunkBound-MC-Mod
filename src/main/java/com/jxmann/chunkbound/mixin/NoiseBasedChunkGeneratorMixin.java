package com.jxmann.chunkbound.mixin;

import com.jxmann.chunkbound.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    @Inject(method = "fillFromNoise", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipNoise(
        Executor executor,
        Blender blender,
        RandomState randomState,
        StructureManager structureManager,
        ChunkAccess chunk,
        CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        ServerLevel level = chunkbound$level(structureManager);

        if (level != null && WorldEvents.shouldBeVoid(level, chunk.getPos())) {
            cir.setReturnValue(CompletableFuture.completedFuture(chunk));
        }
    }

    @Inject(method = "buildSurface", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipSurface(
        WorldGenRegion region,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess chunk,
        CallbackInfo ci
    ) {
        if (WorldEvents.shouldBeVoid(region.getLevel(), chunk.getPos())) {
            ci.cancel();
        }
    }

    @Inject(method = "applyCarvers", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipCarvers(
        WorldGenRegion region,
        long seed,
        RandomState randomState,
        BiomeManager biomeManager,
        StructureManager structureManager,
        ChunkAccess chunk,
        GenerationStep.Carving step,
        CallbackInfo ci
    ) {
        if (WorldEvents.shouldBeVoid(region.getLevel(), chunk.getPos())) {
            ci.cancel();
        }
    }

    private static ServerLevel chunkbound$level(StructureManager structureManager) {
        LevelAccessor level = ((StructureManagerAccessor) structureManager).chunkbound$getLevel();

        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }

        if (level instanceof WorldGenRegion region) {
            return region.getLevel();
        }

        return null;
    }
}
