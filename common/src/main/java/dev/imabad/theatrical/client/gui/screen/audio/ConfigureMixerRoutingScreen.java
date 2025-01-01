package dev.imabad.theatrical.client.gui.screen.audio;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.network.audio.AudioChannelDefinition;
import dev.imabad.theatrical.api.network.audio.AudioChannelType;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.blockentities.sound.MixerBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.audio.MixerChannelListWidget;
import dev.imabad.theatrical.client.gui.widgets.audio.MixerDeviceChannelListWidget;
import dev.imabad.theatrical.client.gui.widgets.audio.MixerDeviceListWidget;
import dev.imabad.theatrical.net.sound.ConfigureMixerPacket;
import dev.imabad.theatrical.util.DimensionBlockPos;
import dev.imabad.theatrical.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ConfigureMixerRoutingScreen extends Screen {

    private int xCenter, yCenter;
    private final MixerBlockEntity mixer;
    private UUID networkId;
    private final Screen lastScreen;
    private GridLayout layout;
    private Map<DimensionBlockPos, AudioDeviceDefinition> knownDevices;
    private MixerChannelListWidget channelList;
    private MixerDeviceListWidget devicesList;
    private MixerDeviceChannelListWidget deviceChannelsList;

    public ConfigureMixerRoutingScreen(Screen lastScreen, MixerBlockEntity mixerBlockEntity, Map<DimensionBlockPos, AudioDeviceDefinition> knownDevices) {
        super(Component.translatable("button.artnetconfig"));
        this.networkId = TheatricalClient.getArtNetManager().getNetworkId();
        this.lastScreen = lastScreen;
        this.mixer = mixerBlockEntity;
        this.knownDevices = knownDevices;
    }

    @Override
    protected void init() {
        super.init();
        layout = new GridLayout();
//        layout = LinearLayout.vertical();
        layout.defaultCellSetting().alignHorizontallyCenter().padding(5);
        xCenter = (this.width / 2);
        yCenter = (this.height / 2);
        channelList = new MixerChannelListWidget(Minecraft.getInstance(), this, 150, 300, Component.literal("test"));
        layout.addChild(channelList, 2, 1, 4, 1, LayoutSettings.defaults().alignHorizontallyCenter().paddingBottom(0));
        Int2ObjectMap<AudioChannelDefinition> allDefs = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < mixer.getDefinition().channelDefinitions().size(); i++) {
            allDefs.put(i, mixer.getDefinition().channelDefinitions().get(i));
        }
        channelList.setEntries(allDefs);
        devicesList = new MixerDeviceListWidget(Minecraft.getInstance(), this, 150, 300, Component.literal("test"));
        layout.addChild(devicesList, 2, 3, 4, 1);
        devicesList.setEntries(knownDevices);
        deviceChannelsList = new MixerDeviceChannelListWidget(Minecraft.getInstance(), this, 150, 300, Component.literal("test"));
        layout.addChild(deviceChannelsList, 2, 5, 4, 2);
        deviceChannelsList.setEntries(new HashMap<>());
        layout.addChild(new CycleButton.Builder<UUID>((networkId) ->
        {
            if (TheatricalClient.getArtNetManager().getKnownNetworks().containsKey(networkId)) {
                return Component.literal(TheatricalClient.getArtNetManager().getKnownNetworks().get(networkId));
            }
            return Component.literal("Unknown");
        }
        ).withValues(CycleButton.ValueListSupplier.create(Stream.concat(Stream.of(UUIDUtil.NULL),
                        TheatricalClient.getArtNetManager().getKnownNetworks().keySet().stream()).collect(Collectors.toList())))
                .displayOnlyValue().withInitialValue(networkId)
                .create(xCenter, yCenter, 150, 20,
                        Component.translatable("screen.artnetconfig.enabled"), (obj, val) -> {
                            this.networkId = val;
                        }), 6, 3);
        layout.addChild(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                        .pos(xCenter + 40, yCenter + 200)
                        .size(150, 20)
                        .build(),
                7, 3
        );
        layout.addChild(
                new Button.Builder(Component.translatable("gui.back"), button -> {
                    this.minecraft.setScreen(this.lastScreen);
                })
                        .pos(xCenter + 40, yCenter + 200)
                        .size(150, 20)
                        .build(),
                7, 1
        );
        this.addRenderableWidget(channelList);
        this.addRenderableWidget(devicesList);
        this.addRenderableWidget(deviceChannelsList);
        layout.arrangeElements();
        this.repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    protected void repositionElements() {
        FrameLayout.alignInRectangle(this.layout, 0, this.height / 6 - 12, this.width, this.height, 0.5F, 0.0F);
    }

    protected void refresh() {
    }

    private void saveCurrentSelection() {
    }

    public void setSelectedDevice(MixerDeviceListWidget.Entry entry){
        this.devicesList.setSelectedEntry(entry);
        if(entry != null) {
            List<AudioChannelDefinition> channelDefs = entry.getDevice().channelDefinitions();
            this.deviceChannelsList.setEntries(IntStream.range(0, channelDefs.size()).boxed().collect(
                    Collectors.toMap(Function.identity(), channelDefs::get)
            ));
        }
    }

    public void setSelectedDeviceChannel(MixerDeviceChannelListWidget.Entry entry){
        this.deviceChannelsList.setSelectedEntry(entry);
        if(entry != null) {
            // Send packet?!
            int channelToChange = this.channelList.getSelected().getAudioDeviceChannel().index();
            AudioChannelType audioChannelType = this.channelList.getSelected().getAudioDeviceChannel().channelType();
            new ConfigureMixerPacket(
                    DimensionBlockPos.of(mixer.getLevel().dimension(), mixer.getBlockPos()),
                    channelToChange,
                    audioChannelType,
                    devicesList.getSelected().getPos(),
                    entry.getChannelDef().index()
            ).sendToServer();
        }
    }

    public void setSelected(MixerChannelListWidget.Entry entry) {
        if (entry != null) {
            saveCurrentSelection();
        }
        this.channelList.setSelectedEntry(entry);
        AudioChannelDefinition audioDeviceChannel = entry.getAudioDeviceChannel();
        Int2ObjectMap<MixerBlockEntity.AudioDeviceChannel> channels;
        if(audioDeviceChannel.channelType() == AudioChannelType.INPUT) {
            channels = mixer.getMixerConfiguration().inputs();
        } else {
            channels = mixer.getMixerConfiguration().outputs();
        }
        MixerBlockEntity.AudioDeviceChannel deviceChannel = channels.get(audioDeviceChannel.index());
        if(deviceChannel != null && deviceChannel.devicePos() != null) {
            Optional<MixerDeviceListWidget.Entry> first = devicesList.children().stream()
                    .filter(entry1 -> entry1.getPos().equals(deviceChannel.devicePos())).findFirst();
            MixerDeviceListWidget.Entry entry1 = first.orElse(null);
            setSelectedDevice(entry1);
            if(entry1 != null) {
                Optional<MixerDeviceChannelListWidget.Entry> deviceChannelOpt = deviceChannelsList.children().stream()
                        .filter(entry2 -> entry2.getChannel() == deviceChannel.channel()).findFirst();
                deviceChannelOpt.ifPresentOrElse((e) -> this.deviceChannelsList.setSelectedEntry(e), () -> {
                    this.deviceChannelsList.setSelectedEntry(null);
                });
            } else {
                this.deviceChannelsList.setSelectedEntry(null);
            }
        } else {
            setSelectedDevice(null);
            this.deviceChannelsList.setSelectedEntry(null);
        }
    }

    private void update() {
        try {

            this.minecraft.setScreen(this.lastScreen);
        } catch (NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics);
    }


    private void renderLabels(GuiGraphics guiGraphics) {

    }

    private void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY, Object... replacements) {
        MutableComponent translatable = Component.translatable(translationKey, replacements);
        guiGraphics.drawString(font, translatable, xCenter + (this.font.width(translatable.getString()) / 2) + offSetX, offSetY, 0xffffff, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}