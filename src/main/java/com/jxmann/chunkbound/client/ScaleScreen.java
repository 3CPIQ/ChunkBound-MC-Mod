package com.jxmann.chunkbound.client;

import com.jxmann.chunkbound.PendingScale;
import com.jxmann.chunkbound.ScaleMode;
import com.jxmann.chunkbound.ScaleSelection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ScaleScreen extends Screen {
    private final Screen parent;
    private ScaleMode mode;
    private boolean otherDimensions;
    private EditBox widthBox;
    private EditBox lengthBox;
    private Button modeButton;
    private Button dimensionsButton;

    public ScaleScreen(Screen parent) {
        super(Component.literal("World Scale"));
        this.parent = parent;
        ScaleSelection selection = PendingScale.get();
        mode = selection.mode();
        otherDimensions = selection.otherDimensions();
    }

    @Override
    protected void init() {
        int center = width / 2;
        ScaleSelection selection = PendingScale.get();
        modeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            mode = mode.next();
            refresh();
        }).bounds(center - 100, 64, 200, 20).build());
        widthBox = new EditBox(font, center - 100, 112, 95, 20, Component.literal("Width in chunks"));
        widthBox.setFilter(this::validNumber);
        widthBox.setValue(Integer.toString(selection.width()));
        addRenderableWidget(widthBox);
        lengthBox = new EditBox(font, center + 5, 112, 95, 20, Component.literal("Length in chunks"));
        lengthBox.setFilter(this::validNumber);
        lengthBox.setValue(Integer.toString(selection.length()));
        addRenderableWidget(lengthBox);
        dimensionsButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            otherDimensions = !otherDimensions;
            refresh();
        }).bounds(center - 100, 156, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> saveAndClose())
            .bounds(center - 100, height - 52, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
            .bounds(center + 5, height - 52, 95, 20).build());
        refresh();
    }

    private void refresh() {
        modeButton.setMessage(Component.literal("Scale: " + mode.label()));
        dimensionsButton.setMessage(Component.literal("Apply to other dimensions: " + (otherDimensions ? "On" : "Off")));
        boolean custom = mode == ScaleMode.CUSTOM;
        widthBox.setEditable(custom);
        widthBox.setTextColor(custom ? 0xE0E0E0 : 0x707070);
        lengthBox.setEditable(custom);
        lengthBox.setTextColor(custom ? 0xE0E0E0 : 0x707070);
    }

    private boolean validNumber(String value) {
        if (value.isEmpty()) {
            return true;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 1 && parsed <= 1874999;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void saveAndClose() {
        int widthValue = parse(widthBox.getValue(), 16);
        int lengthValue = parse(lengthBox.getValue(), 16);
        PendingScale.set(new ScaleSelection(mode, widthValue, lengthValue, otherDimensions));
        minecraft.setScreen(parent);
    }

    private int parse(String value, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        graphics.drawString(font, "World type", width / 2 - 100, 51, 0xA0A0A0, false);
        graphics.drawString(font, "Width", width / 2 - 100, 99, 0xA0A0A0, false);
        graphics.drawString(font, "Length", width / 2 + 5, 99, 0xA0A0A0, false);
        if (mode == ScaleMode.OLD) {
            graphics.drawCenteredString(font, "Old worlds are fixed at 16 x 16 chunks", width / 2, 136, 0xA0A0A0);
        } else if (mode == ScaleMode.CLASSIC) {
            graphics.drawCenteredString(font, "Classic worlds generate infinitely", width / 2, 136, 0xA0A0A0);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
