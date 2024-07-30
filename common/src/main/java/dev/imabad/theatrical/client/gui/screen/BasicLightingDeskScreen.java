package dev.imabad.theatrical.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.FaderWidget;
import dev.imabad.theatrical.net.*;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicLightingDeskScreen extends Screen {

    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/lighting_console.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private BasicLightingDeskBlockEntity be;
    private EditBox fadeInTime, fadeOutTime;
    private UUID networkId;
    public BasicLightingDeskScreen(BasicLightingDeskBlockEntity blockEntity) {
        super(Component.translatable("screen.basicLightingDesk"));
        this.imageWidth = 244;
        this.imageHeight = 126;
        this.be = blockEntity;
        this.networkId = be.getNetworkId();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
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
        renderLabel(guiGraphics, "ui.control.step", 20, 57, be.getCurrentStep());
        renderLabel(guiGraphics, be.isRunMode() ? "ui.control.modes.run" : "ui.control.modes.program", 41, 90);
        renderLabel(guiGraphics, "ui.control.cues", 100, 5);
        for(int key : be.getStoredSteps().keySet()){
            renderLabel(guiGraphics, "ui.control.cue", 101, 15 + (10 * key), key);
        }
        renderLabel(guiGraphics, "ui.control.fadeIn", 35, 10);
        renderLabel(guiGraphics, "ui.control.fadeOut", 35, 33);
    }

    private void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY, Object... replacements) {
        guiGraphics.pose().pushPose();
//        guiGraphics.pose().scale(0.8f, 0.8f, 0.8f);
        MutableComponent translatable = Component.translatable(translationKey, replacements);
        guiGraphics.drawString(font, translatable, (xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2)) + offSetX, yCenter + offSetY, 0x404040, false);
        guiGraphics.pose().popPose();
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
                .pos(xCenter + 155, yCenter + 67)
                .size(15, 20)
                .build());
        this.addRenderableWidget(new Button.Builder(Component.literal("->"), button -> this.moveStep(true))
                .pos(xCenter + 170, yCenter + 67)
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
        this.fadeInTime = new EditBox(this.font, xCenter + 147, yCenter + 20, 20, 10, Component.literal("0"));
        this.fadeOutTime = new EditBox(this.font, xCenter + 147, yCenter + 43, 20, 10, Component.literal("0"));
        this.fadeInTime.setValue(Integer.toString(be.getFadeInTicks()));
        this.fadeOutTime.setValue(Integer.toString(be.getFadeOutTicks()));
        this.addRenderableWidget(fadeInTime);
        this.addRenderableWidget(fadeOutTime);
        this.addRenderableWidget(new CycleButton.Builder<UUID>((networkId) ->
        {
            if (TheatricalClient.getArtNetManager().getKnownNetworks().containsKey(networkId)) {
                return Component.literal(TheatricalClient.getArtNetManager().getKnownNetworks().get(networkId));
            }
            return Component.literal("Unknown");
        }
        ).withValues(CycleButton.ValueListSupplier.create(Stream.concat(Stream.of(UUIDUtil.NULL),
                        TheatricalClient.getArtNetManager().getKnownNetworks().keySet().stream()).collect(Collectors.toList())))
                .displayOnlyValue().withInitialValue(networkId)
                .create(xCenter + 45, yCenter + 130, 150, 20,
                        Component.translatable("screen.artnetconfig.network"), (obj, val) -> {
                            this.networkId = val;
                            new UpdateNetworkId(be.getBlockPos(), networkId).sendToServer();
                        }));
    }

    private void moveStep(boolean forward){
        new ControlMoveStep(be.getBlockPos(), forward).sendToServer();
    }

    private void go(){
        new ControlGo(be.getBlockPos(), Integer.parseInt(fadeInTime.getValue()), Integer.parseInt(fadeOutTime.getValue())).sendToServer();
    }

    private void mode(){
        new ControlModeToggle(be.getBlockPos()).sendToServer();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.children().forEach(widget -> {
            if(widget instanceof FaderWidget fader) {
                if (fader.isMouseOver(mouseX, mouseY) && fader.isDragging()) {
                    int newVal = fader.updateValue(mouseY);
                    new ControlUpdateFader(be.getBlockPos(), fader.getChannel(), newVal).sendToServer();
                }
            }
        });
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
