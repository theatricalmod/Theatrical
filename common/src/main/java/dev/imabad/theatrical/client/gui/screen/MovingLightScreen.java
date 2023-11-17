package dev.imabad.theatrical.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.net.UpdateDMXFixture;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class MovingLightScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/blank.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private EditBox dmxAddress;
    private MovingLightBlockEntity be;

    public MovingLightScreen(MovingLightBlockEntity be) {
        super(Component.translatable("screen.movinglight"));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.be = be;
    }

    @Override
    protected void init() {
        super.init();
        xCenter = (this.width - this.imageWidth) / 2;
        yCenter = (this.height - this.imageHeight) / 2;
        this.dmxAddress = new EditBox(this.font, xCenter + 62, yCenter + 25, 50, 10, (Component)Component.translatable("fixture.dmxStart"));
        this.dmxAddress.setValue(Integer.toString(this.be.getChannelStart()));
        this.addWidget(this.dmxAddress);
        this.addRenderableWidget(new Button(xCenter + 40,  yCenter + 90, 100, 20, Component.translatable("artneti.save"), button -> this.update()));
    }

    private void update(){
        try {
            int dmx = Integer.parseInt(this.dmxAddress.getValue());
            if (dmx > 512 || dmx < 0) {
                return;
            }
            new UpdateDMXFixture(be.getBlockPos(), dmx).sendToServer();
        } catch(NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.renderWindow(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.dmxAddress.render(poseStack, mouseX, mouseY, partialTick);
        this.renderLabels(poseStack);
    }

    private void renderWindow(PoseStack poseStack){
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void renderLabels(PoseStack poseStack) {
        renderLabel(poseStack, "block.theatrical.moving_light", 5,5);
        renderLabel(poseStack, "fixture.dmxStart", 0,15);
    }

    private void renderLabel(PoseStack stack, String translationKey, int offSetX, int offSetY){
        MutableComponent translatable = Component.translatable(translationKey);
        this.font.draw(stack, translatable, xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2), yCenter + offSetY, 0x404040);
    }

    @Override
    public void tick() {
        this.dmxAddress.tick();
    }
}
