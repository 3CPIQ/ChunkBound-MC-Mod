package com.jxmann.chunkbound.client;

import com.jxmann.chunkbound.PendingScale;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEvents {
    private static final Set<CreateWorldScreen> OPENED_SCREENS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<CreateWorldScreen, Button> SCALE_BUTTONS = new WeakHashMap<>();

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
        Button button = Button.builder(Component.literal("Change Scale"), value -> screen.getMinecraft().setScreen(new ScaleScreen(screen)))
            .bounds(0, 0, 100, 20)
            .build();
        button.visible = false;
        SCALE_BUTTONS.put(screen, button);
        event.addListener(button);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) {
            return;
        }
        Button scaleButton = SCALE_BUTTONS.get(screen);
        if (scaleButton == null) {
            return;
        }
        boolean worldTab = screen.children().stream()
            .filter(EditBox.class::isInstance)
            .map(EditBox.class::cast)
            .anyMatch(box -> box.getY() > 120 && box.getWidth() > 250);
        scaleButton.visible = worldTab;
        if (!worldTab) {
            return;
        }
        screen.children().stream()
            .filter(CycleButton.class::isInstance)
            .map(CycleButton.class::cast)
            .max(Comparator.comparingInt(AbstractWidget::getY))
            .ifPresent(bonusChest -> {
                scaleButton.setX(bonusChest.getX());
                scaleButton.setY(bonusChest.getY() + bonusChest.getHeight() + 8);
                scaleButton.setWidth(Math.max(100, bonusChest.getWidth()));
            });
    }
}
