package com.jxmann.chunkbound.client;

import com.jxmann.chunkbound.PendingScale;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
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
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
            return;
        }
        if (OPENED_SCREENS.add(screen)) {
            PendingScale.set(com.jxmann.chunkbound.ScaleSelection.classic());
        }
        Button button = Button.builder(label(), value -> screen.getMinecraft().setScreen(new ScaleScreen(screen)))
            .bounds(screen.width - 126, 6, 120, 20)
            .build();
        event.addListener(button);
    }

    private static Component label() {
        return Component.literal("Scale: " + PendingScale.get().mode().label());
    }
}
