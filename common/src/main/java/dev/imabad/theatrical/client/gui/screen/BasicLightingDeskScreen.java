package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.FaderWidget;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.net.UpdateConsoleFader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class BasicLightingDeskScreen extends Screen {

    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/lighting_console.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private BasicLightingDeskBlockEntity be;
    private EditBox fadeInTime, fadeOutTime;
    public BasicLightingDeskScreen(BasicLightingDeskBlockEntity blockEntity) {
        super(Component.translatable("screen.basicLightingDesk"));
        this.imageWidth = 244;
        this.imageHeight = 126;
        this.be = blockEntity;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderWindow(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics);
    }

    private void renderWindow(GuiGraphics guiGraphics) {
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        renderLabel(guiGraphics, "block.theatrical.led_fresnel", 5, 5);
        renderLabel(guiGraphics, "fixture.dmxStart", 0, 15);
        renderLabel(guiGraphics, "fixture.tilt", 0, 36);
        renderLabel(guiGraphics, "fixture.pan", 0, 66);
    }

    private void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY) {
        MutableComponent translatable = Component.translatable(translationKey);
        guiGraphics.drawString(font, translatable, xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2), yCenter + offSetY, 0x404040, false);
    }

    @Override
    protected void init() {
        super.init();
        xCenter = (this.width - this.imageWidth) / 2;
        yCenter = (this.height - this.imageHeight) / 2;
        byte[] faders = be.getFaders();
        for(int i = 0; i < faders.length; i++){
            int baseY = yCenter + 7;
            if(i >= 6){
                baseY += (i / 6) * 61;
            }
            int faderNumber = i - ((i / 6) * 6);
            this.addRenderableWidget(new FaderWidget(xCenter + 7 + (faderNumber * 20), baseY, i, Byte.toUnsignedInt(faders[i])));
        }
        this.addRenderableWidget(new FaderWidget(xCenter + 184, yCenter + 7, -1, Byte.toUnsignedInt(be.getGrandMaster())));
        this.addRenderableWidget(new Button.Builder(Component.literal("<-"), button -> this.moveStep(false))
                .pos(xCenter + 130, yCenter + 20)
                .size(15, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("->"), button -> this.moveStep(true))
                .pos(xCenter + 145, yCenter + 20)
                .size(15, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("Go"), button -> this.go())
                .pos(xCenter + 130, yCenter + 100)
                .size(20, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("Mode"), button -> this.mode())
                .pos(xCenter + 155, yCenter + 100)
                .size(30, 20)
                .build());
        this.fadeInTime = new EditBox(this.font, xCenter + 170, yCenter + 60, 20, 10, Component.literal("0"));
        this.fadeOutTime = new EditBox(this.font, xCenter + 170, yCenter + 75, 20, 10, Component.literal("0"));
        this.fadeInTime.setValue(Integer.toString(be.getFadeInTicks()));
        this.fadeOutTime.setValue(Integer.toString(be.getFadeOutTicks()));
        this.addRenderableWidget(fadeInTime);
        this.addRenderableWidget(fadeOutTime);
    }

    private void moveStep(boolean forward){

    }

    private void go(){

    }

    private void mode(){

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.children().forEach(widget -> {
            if(widget instanceof FaderWidget fader) {
                if (widget.isMouseOver(mouseX, mouseY) && ((FaderWidget) widget).isDragging()) {
                    int newVal = fader.calculateNewValue(mouseY);
                    new UpdateConsoleFader(be.getBlockPos(), fader.getChannel(), newVal).sendToServer();
                }
            }
        });
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
