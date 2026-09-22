package com.jxmann.chunkbound.client;

import com.jxmann.chunkbound.PendingScale;
import com.jxmann.chunkbound.ScaleSelection;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private static final Set<CreateWorldScreen> OPENED_SCREENS = Collections.newSetFromMap(new WeakHashMap<>());

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen screen && OPENED_SCREENS.add(screen)) {
            PendingScale.set(ScaleSelection.classic());
        }
    }
}
