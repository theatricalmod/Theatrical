package dev.imabad.theatrical.client.gui.screen;

import com.mojang.serialization.Codec;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.gui.widgets.LabeledEditBox;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.net.UpdateArtNetInterface;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtNetConfigurationScreen extends Screen {

    private int xCenter, yCenter;
    private EditBox universeBox1,universeBox2,universeBox3,universeBox4, ipAddressBox;
    private int[] universe;
    private String ipAddress;
    private boolean enabled;
    private UUID networkId;
    private Screen lastScreen;
    private GridLayout layout;

    public ArtNetConfigurationScreen(Screen lastScreen) {
        super(Component.translatable("button.artnetconfig"));
        universe = new int[4];
        universe[0] = TheatricalConfig.INSTANCE.CLIENT.universe1;
        universe[1] = TheatricalConfig.INSTANCE.CLIENT.universe2;
        universe[2] = TheatricalConfig.INSTANCE.CLIENT.universe3;
        universe[3] = TheatricalConfig.INSTANCE.CLIENT.universe4;
        this.ipAddress = TheatricalConfig.INSTANCE.CLIENT.artNetIP;
        this.enabled = TheatricalConfig.INSTANCE.CLIENT.artnetEnabled;
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
        layout.addChild(this.ipAddressBox, 1, 1, 1, 2);
        this.universeBox1 = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, Component.translatable("artneti.dmxUniverse").append(" 1")).color(0xffffff).shadow(true);
        this.universeBox1.setValue(Integer.toString(universe[0]));
        layout.addChild(this.universeBox1,2, 1);
        this.universeBox2 = new LabeledEditBox(this.font, xCenter, yCenter , 100, 20, (Component)Component.translatable("artneti.dmxUniverse").append(" 2")).color(0xffffff).shadow(true);
        this.universeBox2.setValue(Integer.toString(universe[1]));
        layout.addChild(this.universeBox2, 2, 2);
        this.universeBox3 = new LabeledEditBox(this.font, xCenter, yCenter , 100, 20, (Component)Component.translatable("artneti.dmxUniverse").append(" 3")).color(0xffffff).shadow(true);
        this.universeBox3.setValue(Integer.toString(universe[2]));
        layout.addChild(this.universeBox3, 3, 1);
        this.universeBox4 = new LabeledEditBox(this.font, xCenter, yCenter, 100, 20, (Component)Component.translatable("artneti.dmxUniverse").append(" 4")).color(0xffffff).shadow(true);
        this.universeBox4.setValue(Integer.toString(universe[3]));
        layout.addChild(this.universeBox4, 3, 2);
//        new OptionInstance.Enum<>(List.of(true, false), Codec.BOOL).createButton()
        layout.addChild(new CycleButton.Builder<Boolean>((enabled) ->
            Component.translatable("screen.artnetconfig.enabled", enabled ? "Yes" : "No")
        ).withValues(List.of(true, false)).displayOnlyValue().withInitialValue(enabled).create(xCenter, yCenter, 150, 20, Component.translatable("screen.artnetconfig.enabled"), (obj, val) -> {
            this.enabled = val;
        }), 4, 1);
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
                        }), 4, 2);
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
                5, 1
        );
        layout.addChild(
                new Button.Builder(Component.translatable("gui.back"), button -> {
                    this.minecraft.setScreen(this.lastScreen);
                })
                        .pos(xCenter + 40, yCenter + 200)
                        .size(150, 20)
                        .build(),
                5, 2
        );
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }


    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void refresh(){

    }

    private int getValueFor(int universe){
        return switch (universe) {
            case 1 -> Integer.parseInt(this.universeBox1.getValue());
            case 2 -> Integer.parseInt(this.universeBox2.getValue());
            case 3 -> Integer.parseInt(this.universeBox3.getValue());
            case 4 -> Integer.parseInt(this.universeBox4.getValue());
            default -> -1;
        };
    }

    private void setValueFor(int universe, int value){
        switch (universe) {
            case 1:
                TheatricalConfig.INSTANCE.CLIENT.universe1 = value;
                break;
            case 2:
                TheatricalConfig.INSTANCE.CLIENT.universe2 = value;
                break;
            case 3:
                TheatricalConfig.INSTANCE.CLIENT.universe3 = value;
                break;
            case 4:
                TheatricalConfig.INSTANCE.CLIENT.universe4 = value;
                break;
        };
    }

    private void update(){
        try {
            int[] oldUniverses = Arrays.copyOf(universe, 4);
            for(int i = 0; i < universe.length; i++){
                int val = getValueFor(i + 1);
                universe[i] = val;
                setValueFor(i + 1, val);
            }
            boolean hasChangedIP = false;
            if(!Objects.equals(TheatricalConfig.INSTANCE.CLIENT.artNetIP, ipAddressBox.getValue()) && TheatricalConfig.INSTANCE.CLIENT.artNetIP != null){
                hasChangedIP = true;
            }
            TheatricalConfig.INSTANCE.CLIENT.artNetIP = ipAddressBox.getValue();
            TheatricalConfig.INSTANCE.CLIENT.artnetEnabled = enabled;
            if(networkId != TheatricalClient.getArtNetManager().getNetworkId()) {
                TheatricalClient.getArtNetManager().setNetworkId(networkId);

            }
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
                    if(TheatricalClient.getArtNetManager().getClient() != null){
                        for (int oldUnivers : oldUniverses) {
                            boolean found = false;
                            if (oldUnivers >= 0) {
                                for (int x = 0; x < universe.length; x++) {
                                    if (universe[x] == oldUnivers) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    if (TheatricalClient.getArtNetManager().getClient().isSubscribedTo(oldUnivers)) {
                                        TheatricalClient.getArtNetManager().getClient().unsubscribeFromUniverse(oldUnivers);
                                    }
                                }
                            }
                        }
                        for (int j : universe) {
                            if (j >= 0 && !TheatricalClient.getArtNetManager().getClient().isSubscribedTo(j)) {
                                TheatricalClient.getArtNetManager().getClient().subscribeToUniverse(j);
                            }
                        }
                    }
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