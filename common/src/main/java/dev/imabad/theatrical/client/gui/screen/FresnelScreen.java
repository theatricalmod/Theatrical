package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.BasicSlider;
import dev.imabad.theatrical.net.UpdateDMXFixture;
import dev.imabad.theatrical.net.UpdateFixturePosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class FresnelScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/blank.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private EditBox dmxAddress;
    private BasicSlider tiltSlider, panSlider;
    private FresnelBlockEntity be;

    public FresnelScreen(FresnelBlockEntity be) {
        super(Component.translatable("screen.fresnel"));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.be = be;
    }

    @Override
    protected void init() {
        super.init();
        xCenter = (this.width - this.imageWidth) / 2;
        yCenter = (this.height - this.imageHeight) / 2;
        this.dmxAddress = new EditBox(this.font, xCenter + 62, yCenter + 25, 50, 10, Component.translatable("fixture.dmxStart"));
        this.dmxAddress.setValue(Integer.toString(this.be.getChannelStart()));
        this.tiltSlider = new BasicSlider(xCenter + 13, yCenter + 45, 150, 20, Component.empty(), be.getTilt(), -90, 90, (newTilt) -> {
            be.setTilt(newTilt.intValue());
        });
        this.panSlider = new BasicSlider(xCenter + 13, yCenter + 75, 150, 20, Component.empty(), be.getPan(),-180, 180, (newPan) -> {
            be.setPan(newPan.intValue());
        });
        this.addRenderableWidget(this.dmxAddress);
        this.addRenderableWidget(tiltSlider);
        this.addRenderableWidget(panSlider);
        this.addRenderableWidget(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                        .pos(xCenter + 40, yCenter + 100)
                        .size(100, 20)
                        .build()
        );
    }

    private void update() {
        try {
            int dmx = Integer.parseInt(this.dmxAddress.getValue());
            if (dmx > 512 || dmx < 0) {
                return;
            }
            new UpdateDMXFixture(be.getBlockPos(), dmx).sendToServer();
            new UpdateFixturePosition(be.getBlockPos(), be.getTilt(), be.getPan()).sendToServer();
        } catch (NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
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
    public void tick() {
//        this.dmxAddress.tick();
    }
}