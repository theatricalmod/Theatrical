package dev.imabad.theatrical.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class BetterCheckbox extends Checkbox {

    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
    private boolean showLabel = false;

    public BetterCheckbox(int x, int y, Component message, Font font, int width, int height, boolean selected, Checkbox.OnValueChange onValueChange) {
        super(x, y, width, message, font, selected, onValueChange);
        this.width = width;
        this.height = height;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Identifier identifier;
        if (this.selected()) {
            identifier = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            identifier = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int k = getBoxSize(font);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), k, k, ARGB.white(this.alpha));
        if (this.showLabel) {
            int l = this.getX() + k + 4;
            int m = this.getY() + k / 2 - this.textWidget.getHeight() / 2;
            this.textWidget.setPosition(l, m);
            this.textWidget.visitLines(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.notClickable(this.isHovered())));
        }
    }

}
