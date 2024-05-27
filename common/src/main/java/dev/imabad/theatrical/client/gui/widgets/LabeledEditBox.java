package dev.imabad.theatrical.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class LabeledEditBox extends EditBox {
    private Font font;
    public LabeledEditBox(Font font, int width, int height, Component message) {
        super(font, width, height, message);
        this.width = width + 10;
        this.font = font;
    }

    public LabeledEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.font = font;
    }

    public LabeledEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
        super(font, x, y, width, height, editBox, message);
        this.font = font;
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        int k = this.isBordered() ? this.getX() + 4 : this.getX();
        int l = this.isBordered() ? this.getY() + (this.height - 8) / 2 : this.getY();

        guiGraphics.drawString(font, this.getMessage(), k, l - (font.lineHeight * 2), 0xffffff);
    }
}
