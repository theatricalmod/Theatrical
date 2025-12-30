package dev.imabad.theatrical.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imabad.theatrical.Theatrical;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FaderWidget extends AbstractWidget {
    private static final Identifier background = Theatrical.location(
            "textures/gui/lighting_console.png");

    private final int channel;
    private int value;

    private boolean dragging = false;

//    public final IDraggable onDrag;
    public FaderWidget(int x, int y, int channel, int value) {
        super(x, y, 10, 51, Component.empty());
        this.channel = channel;
        this.value = value;
    }

    public int getChannel() {
        return channel;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
//        RenderSystem.();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, getX(), getY(), 0f, 126, getWidth(), getHeight(), 10, 51, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, getX() + 1, (getY() + (height - 7)) - (int) ((this.value / 255f) * 50), 10, 126, 8, 11, 256, 256);
//        RenderSystem.enableDepthTest();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        this.value = calculateNewValue(mouseButtonEvent.y());
        this.dragging = true;
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        this.dragging = false;
    }


    public boolean isDragging() {
        return dragging;
    }
    public int calculateNewValue(double mouseY){
        return (int) (((this.height - (mouseY - this.getY())) / this.height) * 255f);
    }

    public int updateValue(double mouseY){
        this.value = calculateNewValue(mouseY);
        return value;
    }
}
