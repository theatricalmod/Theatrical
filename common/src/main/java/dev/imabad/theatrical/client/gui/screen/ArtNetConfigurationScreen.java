package dev.imabad.theatrical.client.gui.screen;

import com.mojang.serialization.Codec;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.gui.widgets.ArtNetUniverseConfigurationList;
import dev.imabad.theatrical.client.gui.widgets.LabeledEditBox;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.config.UniverseConfig;
import dev.imabad.theatrical.net.UpdateArtNetInterface;
import dev.imabad.theatrical.util.UUIDUtil;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtNetConfigurationScreen extends Screen {

    private int xCenter, yCenter;
    private EditBox ipAddressBox, networkUniverse, subnet, universe;
    private Checkbox universeEnabled;
    private Button deleteConfig;
    private IntObjectMap<UniverseConfig> universeConfigs = new IntObjectHashMap<>();
    private String ipAddress;
    private boolean enabled;
    private UUID networkId;
    private Screen lastScreen;
    private GridLayout layout;
    private ArtNetUniverseConfigurationList configList;

    public ArtNetConfigurationScreen(Screen lastScreen) {
        super(Component.translatable("button.artnetconfig"));
        this.ipAddress = TheatricalConfig.INSTANCE.CLIENT.artNetIP;
        this.enabled = TheatricalConfig.INSTANCE.CLIENT.artnetEnabled;
        this.universeConfigs = new IntObjectHashMap<>();
        TheatricalConfig.INSTANCE.CLIENT.universes.forEach((integer, universeConfig) -> {
            universeConfigs.put(integer, new UniverseConfig(universeConfig.subnet, universeConfig.universe, universeConfig.enabled));
        });
        this.networkId = TheatricalClient.getArtNetManager().getNetworkId();
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        layout = new GridLayout();
//        layout = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL);
        layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        xCenter = (this.width / 2);
        yCenter = (this.height / 2);
        this.ipAddressBox = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, Component.translatable("artneti.ipAddress")).color(0xffffff).shadow(true);
        this.ipAddressBox.setValue(ipAddress);
        layout.addChild(this.ipAddressBox, 1, 1, 1, 4);
        configList = new ArtNetUniverseConfigurationList(Minecraft.getInstance(), this, 150, 200, Component.literal("test"));
        layout.addChild(configList, 2, 1, 2, 1, LayoutSettings.defaults().alignHorizontallyCenter().paddingBottom(0));
        configList.setEntries(universeConfigs);
        layout.addChild(new Button.Builder(Component.literal("Add"), (b) -> {
            universeConfigs.put(universeConfigs.size() + 1, new UniverseConfig(0, 0, false));
            refresh();
        }).width(150).build(), 4, 1, LayoutSettings.defaults().paddingTop(0).alignHorizontallyCenter());
        deleteConfig = new Button.Builder(Component.literal("Delete"), (b) -> {
            if(configList.getSelected() != null) {
                universeConfigs.remove(configList.getSelected().getNetworkUniverse());
                setSelected(null);
            }
            refresh();
        }).width(150).build();
        deleteConfig.active = false;
        deleteConfig.visible = false;
        layout.addChild(deleteConfig, 4, 3, LayoutSettings.defaults().paddingTop(0).alignHorizontallyCenter());
        networkUniverse = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, Component.translatable("screen.artnetconfig.networkUniverse")).color(0xffffff).textOffsetY(-5).shadow(true);
        networkUniverse.visible = false;
        networkUniverse.active = false;
        layout.addChild(networkUniverse, 2, 2);
        universeEnabled = new Checkbox(xCenter, yCenter, 150, 20, Component.translatable("screen.artnetconfig.networkEnabled"), false);
        universeEnabled.visible = false;
        universeEnabled.active = false;
        layout.addChild(universeEnabled, 2, 3);
        subnet = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, Component.translatable("screen.artnetconfig.subnet")).color(0xffffff).textOffsetY(-5).shadow(true);
        subnet.visible = false;
        subnet.active = false;
        layout.addChild(subnet, 3, 2);
        universe = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, Component.translatable("artneti.dmxUniverse")).color(0xffffff).textOffsetY(-5).shadow(true);
        universe.visible = false;
        universe.active = false;
        layout.addChild(universe, 3, 3);
        layout.addChild(new CycleButton.Builder<Boolean>((enabled) ->
            Component.translatable("screen.artnetconfig.enabled", enabled ? "Yes" : "No")
        ).withValues(List.of(true, false)).displayOnlyValue().withInitialValue(enabled).create(xCenter, yCenter, 150, 20, Component.translatable("screen.artnetconfig.enabled"), (obj, val) -> {
            this.enabled = val;
        }), 5, 1);
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
                        }), 5, 3);
//        layout.addChild(new Button.Builder(
//                ,
//                button -> {
//                    enabled = !enabled;
//                }).pos(xCenter + 40, yCenter + 150)
//                .size(100, 20)
//                .build()
//        );
        layout.addChild(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                        .pos(xCenter + 40, yCenter + 200)
                        .size(150, 20)
                        .build(),
                6, 3
        );
        layout.addChild(
                new Button.Builder(Component.translatable("gui.back"), button -> {
                    this.minecraft.setScreen(this.lastScreen);
                })
                        .pos(xCenter + 40, yCenter + 200)
                        .size(150, 20)
                        .build(),
                6, 1
        );
        layout.arrangeElements();
        this.addRenderableWidget(configList);
        layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }


    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void refresh(){
        configList.setEntries(universeConfigs);
    }

    private void saveCurrentSelection(){
        if(this.configList.getSelected() != null) {
            this.configList.getSelected().getConfig().subnet = Integer.parseInt(subnet.getValue());
            this.configList.getSelected().getConfig().universe = Integer.parseInt(universe.getValue());
            this.configList.getSelected().getConfig().enabled = universeEnabled.selected();
            if (this.configList.getSelected().getNetworkUniverse() != Integer.parseInt(networkUniverse.getValue())) {
                int oldAddress = this.configList.getSelected().getNetworkUniverse();
                int newAddress = Integer.parseInt(networkUniverse.getValue());
                universeConfigs.put(newAddress, this.configList.getSelected().getConfig());
                universeConfigs.remove(oldAddress);
                refresh();
            } else {
                universeConfigs.put(this.configList.getSelected().getNetworkUniverse(), this.configList.getSelected().getConfig());
            }
        }
    }

    public void setSelected(ArtNetUniverseConfigurationList.Entry entry){
        if(entry != null) {
            saveCurrentSelection();
        }
        this.configList.setSelected(entry);
        if(entry != null) {
            networkUniverse.visible = true;
            networkUniverse.active = true;
            networkUniverse.setValue(Integer.toString(entry.getNetworkUniverse()));

            universe.visible = true;
            universe.active = true;
            universe.setValue(Integer.toString(entry.getConfig().getUniverse()));

            subnet.visible = true;
            subnet.active = true;
            subnet.setValue(Integer.toString(entry.getConfig().getSubnet()));

            universeEnabled.visible = true;
            universeEnabled.active = true;
            if (!universeEnabled.selected() && entry.getConfig().isEnabled()) {
                universeEnabled.onPress();
            } else if (universeEnabled.selected() && !entry.getConfig().isEnabled()) {
                universeEnabled.onPress();
            }
            deleteConfig.active = true;
            deleteConfig.visible = true;
        } else {
            networkUniverse.visible = false;
            networkUniverse.active = false;

            universe.visible = false;
            universe.active = false;

            subnet.visible = false;
            subnet.active = false;

            universeEnabled.visible = false;
            universeEnabled.active = false;
            deleteConfig.active = false;
            deleteConfig.visible = false;
        }
    }

    private void update(){
        try {
            saveCurrentSelection();
//            int[] oldUniverses = Arrays.copyOf(universe, 4);
//            for(int i = 0; i < universe.length; i++){
//                int val = getValueFor(i + 1);
//                universe[i] = val;
//                setValueFor(i + 1, val);
//            }
            boolean hasChangedIP = false;
            if(!Objects.equals(TheatricalConfig.INSTANCE.CLIENT.artNetIP, ipAddressBox.getValue()) && TheatricalConfig.INSTANCE.CLIENT.artNetIP != null){
                hasChangedIP = true;
            }
            TheatricalConfig.INSTANCE.CLIENT.artNetIP = ipAddressBox.getValue();
            TheatricalConfig.INSTANCE.CLIENT.artnetEnabled = enabled;
            if(networkId != TheatricalClient.getArtNetManager().getNetworkId()) {
                TheatricalClient.getArtNetManager().setNetworkId(networkId);
            }
            TheatricalConfig.INSTANCE.CLIENT.universes = universeConfigs;
            ConfigHandler.INSTANCE.saveConfig(ConfigHandler.ConfigSide.CLIENT);
            boolean isInGame = Minecraft.getInstance().level != null;
            if(isInGame) {
                if (!enabled) {
                    TheatricalClient.getArtNetManager().shutdownAll();
                } else {
                    if (hasChangedIP) {
                        TheatricalClient.getArtNetManager().shutdownAll();
                        TheatricalClient.getArtNetManager().getClient();
                    }
                    if(TheatricalClient.getArtNetManager().getClient() != null) {
                        TheatricalClient.getArtNetManager().getClient().refreshSubscriptions();
                    }
//                        for (int oldUnivers : oldUniverses) {
//                            boolean found = false;
//                            if (oldUnivers >= 0) {
//                                for (int x = 0; x < universe.length; x++) {
//                                    if (universe[x] == oldUnivers) {
//                                        found = true;
//                                        break;
//                                    }
//                                }
//                                if (!found) {
//                                    if (TheatricalClient.getArtNetManager().getClient().isSubscribedTo(oldUnivers)) {
//                                        TheatricalClient.getArtNetManager().getClient().unsubscribeFromUniverse(oldUnivers);
//                                    }
//                                }
//                            }
//                        }
//                        for (int j : universe) {
//                            if (j >= 0 && !TheatricalClient.getArtNetManager().getClient().isSubscribedTo(j)) {
//                                TheatricalClient.getArtNetManager().getClient().subscribeToUniverse(j);
//                            }
//                        }
//                    }
                }
            }
            this.minecraft.setScreen(this.lastScreen);
//            new UpdateArtNetInterface(be.getBlockPos(), ipAddressBox.getValue(), universe).sendToServer();
        } catch(NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderLabels(guiGraphics);
    }


    private void renderLabels(GuiGraphics guiGraphics) {
//        renderLabel(guiGraphics, "screen.artnetconfig.universe", 0,15, 1);
//        renderLabel(guiGraphics, "screen.artnetconfig.universe", 0,15, 2);
//        renderLabel(guiGraphics, "screen.artnetconfig.universe", 0,15, 3);
//        renderLabel(guiGraphics, "screen.artnetconfig.universe", 0,15, 4);
//        renderLabel(guiGraphics, "artneti.ipAddress", 5,10);
//        if(!this.be.isOwnedByCurrentClient()){
//            renderLabel(guiGraphics, "artneti.notAuthorized", 5,75);
//        } else {
//            if(this.be.hasReceivedPacket()){
//                long inSeconds = Math.round((float) (System.currentTimeMillis() - this.be.getLastReceivedPacket()) / 1000);
//                renderLabel(guiGraphics, "artneti.lastReceived", 5,75, inSeconds);
//            } else {
//                renderLabel(guiGraphics, "artneti.notConnected", 5,75);
//            }
//        }
    }

    private void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY, Object... replacements){
        MutableComponent translatable = Component.translatable(translationKey, replacements);
        guiGraphics.drawString(font, translatable, xCenter + (this.font.width(translatable.getString()) / 2), yCenter + offSetY, 0xffffff, false);
    }

    @Override
    public void tick() {
//        this.dmxUniverse.tick();
//        this.ipAddress.tick();
    }
}