package dev.imabad.theatrical.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class LabeledEditBox extends EditBox {
    private float alignX = 0.5F;
    private Font font;

    private int color = 0xffffff;

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
    private LabeledEditBox horizontalAlignment(float horizontalAlignment) {
        this.alignX = horizontalAlignment;
        return this;
    }

    public LabeledEditBox alignLeft() {
        return this.horizontalAlignment(0.0F);
    }

    public LabeledEditBox alignCenter() {
        return this.horizontalAlignment(0.5F);
    }

    public LabeledEditBox alignRight() {
        return this.horizontalAlignment(1.0F);
    }

    public LabeledEditBox color(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        Component component = this.getMessage();
        int i = this.getWidth();
        int j = font.width(component);
        int k = this.getX() + Math.round(this.alignX * (float)(i - j));
        int l = this.getY() + (this.getHeight() - 9) / 2;
        // j > i ? this.clipText(component, i) :
        FormattedCharSequence formattedCharSequence =  component.getVisualOrderText();
        guiGraphics.drawString(font, formattedCharSequence, k, l - (font.lineHeight), color);
    }

    private FormattedCharSequence clipText(Component message, int width) {
        Font font = this.font;
        FormattedText formattedText = font.substrByWidth(message, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedText, CommonComponents.ELLIPSIS));
    }
}
