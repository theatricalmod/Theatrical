package dev.imabad.theatrical.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class BetterStringWidget extends StringWidget {
    private float alignX = 0.5F;
    private boolean shadow = true;

    public BetterStringWidget(Component message, Font font) {
        super(message, font);
    }

    public BetterStringWidget setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public BetterStringWidget setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int i = this.getWidth();
        int j = font.width(component);
        int k = this.getX() + Math.round(this.alignX * (float)(i - j));
        int l = this.getY() + (this.getHeight() - 9) / 2;
        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, k, l, this.getColor(), shadow);
    }


}
