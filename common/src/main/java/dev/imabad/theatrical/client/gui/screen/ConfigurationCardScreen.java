package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.gui.widgets.BetterStringWidget;
import dev.imabad.theatrical.client.gui.widgets.LabeledEditBox;
import dev.imabad.theatrical.net.ConfigureConfigurationCard;
import dev.imabad.theatrical.net.UpdateDMXFixture;
import dev.imabad.theatrical.net.UpdateNetworkId;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationCardScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/blank.png");
    protected final int imageWidth;
    protected final int imageHeight;
    protected int xCenter;
    protected int yCenter;
    protected LinearLayout layout;
    private LabeledEditBox dmxAddress, dmxUniverse;
    private Checkbox autoIncrement;
    private UUID networkId = UUIDUtil.NULL;
    private CompoundTag itemData;
    public ConfigurationCardScreen(CompoundTag itemData) {
        super(Component.translatable("screen.configurationcard"));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.itemData = itemData;
        if(itemData.hasUUID("network")) {
            this.networkId = itemData.getUUID("network");
        }
    }
    @Override
    protected void init() {
        super.init();
        layout = new LinearLayout(imageWidth, imageHeight + 50, LinearLayout.Orientation.VERTICAL);
        layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        layout.addChild(new BetterStringWidget(Component.translatable("screen.configurationcard"), this.font).setColor(4210752).setShadow(false));
        this.dmxUniverse = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("artneti.dmxUniverse"));
        if(itemData.contains("dmxUniverse")){
            this.dmxUniverse.setValue(Integer.toString(itemData.getInt("dmxUniverse")));
        } else {
            this.dmxUniverse.setValue("0");
        }
        layout.addChild(dmxUniverse);
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
                        Component.translatable("screen.artnetconfig.network"), (obj, val) -> {
                            this.networkId = val;
                        }));

        this.dmxAddress = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("fixture.dmxStart"));
        if(itemData.contains("dmxAddress")){
            this.dmxAddress.setValue(Integer.toString(itemData.getInt("dmxAddress")));
        }else {
            this.dmxAddress.setValue("0");
        }
        layout.addChild(dmxAddress);
        this.autoIncrement = new Checkbox(xCenter, yCenter, 150, 20, Component.translatable("screen.configurationcard.autoincrement"), itemData.getBoolean("autoIncrement"));

        layout.addChild(autoIncrement);
        layout.addChild(
                new Button.Builder(Component.translatable("artneti.save"), button -> this.update())
                        .pos(xCenter, yCenter)
                        .size(100, 20)
                        .build()
        );
        refreshLayout();
        this.repositionElements();

    }
    protected void refreshLayout(){
        if(layout == null)
            return;
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    protected void repositionElements() {
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void update(){
        try {
            int dmx = Integer.parseInt(this.dmxAddress.getValue());
            if (dmx > 512 || dmx < 0) {
                return;
            }
            int universe = Integer.parseInt(this.dmxUniverse.getValue());
            if (universe > 16 || universe < 0) {
                return;
            }
            new ConfigureConfigurationCard(networkId, dmx, universe, autoIncrement.selected()).sendToServer();
            Minecraft.getInstance().setScreen(null);
        } catch(NumberFormatException ignored) {
            //We need a nicer way to show that this is invalid?
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderWindow(guiGraphics);
    }

    private void renderWindow(GuiGraphics guiGraphics){
        int layoutHeight = 0;
        if(layout != null) {
            layoutHeight = layout.getHeight();
        }
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - layoutHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, imageWidth, layoutHeight, 0, 0, this.imageWidth, this.imageHeight, 256,256);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
