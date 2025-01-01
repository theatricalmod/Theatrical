package dev.imabad.theatrical.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imabad.theatrical.Theatrical;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SliderWidget extends AbstractWidget {
    private static final ResourceLocation background = new ResourceLocation(Theatrical.MOD_ID,
            "textures/gui/slider.png");

    private final int channel;
    private float value;
    private final float maxValue;

    private boolean dragging = false;
    public SliderWidget(int x, int y, int channel, float value, float maxValue) {
        super(x, y, 10, 51, Component.empty());
        this.channel = channel;
        this.value = value;
        this.maxValue = maxValue;
    }
    public SliderWidget(int x, int y, int channel, int value) {
        this(x, y, channel, value, 255);
    }

    public int getChannel() {
        return channel;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
        RenderSystem.disableDepthTest();
        guiGraphics.blit(background, getX(), getY(), getWidth(), getHeight(), 0, 0, 10, 51, 64, 64);
        guiGraphics.blit(background, getX() + 1, (getY() + (height - 7)) - (int) ((this.value / maxValue) * 50), 8, 11, 10, 0, 8, 11, 64, 64);
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.value = calculateNewValue(mouseY);
        this.dragging = true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public float calculateNewValue(double mouseY){
        return (float) (((this.height - (mouseY - this.getY())) / this.height) * maxValue);
    }

    public float updateValue(double mouseY){
        this.value = calculateNewValue(mouseY);
        return value;
    }
}
