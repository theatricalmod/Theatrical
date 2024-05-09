package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.net.UpdateArtNetInterface;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ArtNetInterfaceScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/blank.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private EditBox dmxUniverse, ipAddress;
    private ArtNetInterfaceBlockEntity be;

    public ArtNetInterfaceScreen(ArtNetInterfaceBlockEntity be) {
        super(Component.translatable("screen.artnetinterface"));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.be = be;
    }

    @Override
    protected void init() {
        super.init();
        xCenter = (this.width - this.imageWidth) / 2;
        yCenter = (this.height - this.imageHeight) / 2;
        this.dmxUniverse = new EditBox(this.font, xCenter + 62, yCenter + 25, 50, 10, (Component)Component.translatable("artneti.dmxUniverse"));
        this.dmxUniverse.setValue(Integer.toString(this.be.getUniverse()));
        this.dmxUniverse.setEditable(be.isOwnedByCurrentClient());
        this.addWidget(this.dmxUniverse);
        this.ipAddress = new EditBox(this.font, xCenter + 40, yCenter + 50, 100, 20, (Component)Component.translatable("artneti.ipAddress"));
        this.ipAddress.setValue(this.be.getIp());
        this.ipAddress.setEditable(be.isOwnedByCurrentClient());
        this.addWidget(this.ipAddress);
        if(be.isOwnedByCurrentClient()) {
            this.addRenderableWidget(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                    .pos(xCenter + 40, yCenter + 90)
                    .size(100, 20)
                    .build()
            );
        }
    }

    private void update(){
        try {
            int dmx = Integer.parseInt(this.dmxUniverse.getValue());
            if (dmx > 512 || dmx < 0) {
                return;
            }
            new UpdateArtNetInterface(be.getBlockPos(), ipAddress.getValue(), dmx).sendToServer();
        } catch(NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        this.renderWindow(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.dmxUniverse.render(guiGraphics, mouseX, mouseY, partialTick);
        this.ipAddress.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics);
    }

    private void renderWindow(GuiGraphics guiGraphics){
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        renderLabel(guiGraphics, "block.theatrical.artnet_interface", 5,5);
        renderLabel(guiGraphics, "artneti.dmxUniverse", 0,15);
        renderLabel(guiGraphics, "artneti.ipAddress", 5,40);
        if(!this.be.isOwnedByCurrentClient()){
            renderLabel(guiGraphics, "artneti.notAuthorized", 5,75);
        } else {
            if(this.be.hasReceivedPacket()){
                long inSeconds = Math.round((float) (System.currentTimeMillis() - this.be.getLastReceivedPacket()) / 1000);
                renderLabel(guiGraphics, "artneti.lastReceived", 5,75, inSeconds);
            } else {
                renderLabel(guiGraphics, "artneti.notConnected", 5,75);
            }
        }
    }

    private void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY, Object... replacements){
        MutableComponent translatable = Component.translatable(translationKey, replacements);
        guiGraphics.drawString(font, translatable, xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2), yCenter + offSetY, 0x404040, false);
    }

    @Override
    public void tick() {
//        this.dmxUniverse.tick();
//        this.ipAddress.tick();
    }
}
