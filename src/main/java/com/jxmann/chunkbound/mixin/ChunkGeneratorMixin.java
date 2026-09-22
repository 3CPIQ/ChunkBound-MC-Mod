package com.jxmann.chunkbound.mixin;

import com.jxmann.chunkbound.WorldEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipDecoration(
        WorldGenLevel level,
        ChunkAccess chunk,
        StructureManager structureManager,
        CallbackInfo ci
    ) {
        ServerLevel serverLevel = chunkbound$level(level);

        if (serverLevel != null && WorldEvents.shouldBeVoid(serverLevel, chunk.getPos())) {
            ci.cancel();
        }
    }

    @Inject(method = "createReferences", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipReferences(
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkAccess chunk,
        CallbackInfo ci
    ) {
        ServerLevel serverLevel = chunkbound$level(level);

        if (serverLevel != null && WorldEvents.shouldBeVoid(serverLevel, chunk.getPos())) {
            ci.cancel();
        }
    }

    @Inject(method = "createStructures", at = @At("HEAD"), cancellable = true)
    private void chunkbound$skipStructures(
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager,
        ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager,
        CallbackInfo ci
    ) {
        LevelAccessor level = ((StructureManagerAccessor) structureManager).chunkbound$getLevel();
        ServerLevel serverLevel = chunkbound$level(level);

        if (serverLevel != null && WorldEvents.shouldBeVoid(serverLevel, chunk.getPos())) {
            ci.cancel();
        }
    }

    private static ServerLevel chunkbound$level(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel;
        }

        if (level instanceof WorldGenRegion region) {
            return region.getLevel();
        }

        return null;
    }
}
