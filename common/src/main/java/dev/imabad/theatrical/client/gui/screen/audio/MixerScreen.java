package dev.imabad.theatrical.client.gui.screen.audio;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.network.audio.AudioChannelType;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.blockentities.sound.MixerBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.SliderWidget;
import dev.imabad.theatrical.net.*;
import dev.imabad.theatrical.net.sound.SetMixerVolumePacket;
import dev.imabad.theatrical.util.DimensionBlockPos;
import dev.imabad.theatrical.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MixerScreen extends Screen {

    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/lighting_console.png");

    private int imageWidth, imageHeight, xCenter, yCenter;
    private MixerBlockEntity be;
    private Map<DimensionBlockPos, AudioDeviceDefinition> knownDevices;
    private UUID networkId;

    public MixerScreen(MixerBlockEntity blockEntity, Map<DimensionBlockPos, AudioDeviceDefinition> knownDevices) {
        super(Component.translatable("screen.basicLightingDesk"));
        this.knownDevices = knownDevices;
        this.imageWidth = 244;
        this.imageHeight = 126;
        this.be = blockEntity;
        this.networkId = be.getNetworkId();
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
        Int2ObjectMap<MixerBlockEntity.AudioDeviceChannel> inputs = be.getMixerConfiguration().inputs();
        for(int i = 0; i < inputs.size(); i++){
            int baseY = yCenter + 7;
            if(i >= 6){
                baseY += (i / 6) * 61;
            }
            int faderNumber = i - ((i / 6) * 6);
            this.addRenderableWidget(new SliderWidget(xCenter + 7 + (faderNumber * 20), baseY, i, inputs.get(i).getGain(), 2f));
        }

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
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.mixer.configure"),
                                (btn) -> goToConfigureScreen())
                        .pos(xCenter + 45, yCenter + 160)
                        .size(150, 20).build());
    }

    public void goToConfigureScreen(){
        this.minecraft.setScreen(new ConfigureMixerRoutingScreen(this, be, knownDevices));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        this.children().forEach(widget -> {
            if(widget instanceof SliderWidget fader) {
                if (fader.isMouseOver(mouseX, mouseY) && fader.isDragging()) {
                    float newVal = fader.updateValue(mouseY);
                    new SetMixerVolumePacket(DimensionBlockPos.of(be.getLevel().dimension(), be.getBlockPos()),
                            AudioChannelType.INPUT,
                            fader.getChannel(), newVal).sendToServer();
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
