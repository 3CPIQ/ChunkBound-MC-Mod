package com.jxmann.chunkbound.mixin;

import com.jxmann.chunkbound.client.ScaleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class CreateWorldScreenWorldTabMixin extends GridLayoutTab {
    protected CreateWorldScreenWorldTabMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chunkbound$addScaleButton(CreateWorldScreen screen, CallbackInfo ci) {
        Button button = Button.builder(
            Component.literal("Scale"),
            value -> screen.getMinecraft().setScreen(new ScaleScreen(screen))
        ).width(308).build();

        this.layout.addChild(
            button,
            5,
            0,
            1,
            2,
            this.layout.newCellSettings().alignHorizontallyCenter()
        );
    }
}
