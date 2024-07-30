package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.client.gui.widgets.LabeledEditBox;
import dev.imabad.theatrical.net.UpdateDMXFixture;
import dev.imabad.theatrical.net.UpdateNetworkId;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericDMXConfigurationScreen<T extends DMXConsumer> extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Theatrical.MOD_ID, "textures/gui/blank.png");

    protected final int imageWidth;
    protected final int imageHeight;
    protected int xCenter;
    protected int yCenter;
    private LabeledEditBox dmxAddress, dmxUniverse;
    private UUID networkId;
    private final T be;
    private final BlockPos blockPos;
    private final String titleTranslationKey;
    protected LinearLayout layout;

    public GenericDMXConfigurationScreen(T be, BlockPos pos, String titleTranslationKey) {
        super(Component.translatable(titleTranslationKey));
        this.imageWidth = 176;
        this.imageHeight = 126;
        this.be = be;
        this.blockPos = pos;
        this.titleTranslationKey = titleTranslationKey;
        this.networkId = be.getNetworkId();
    }

    public void addExtraWidgetsToUI(){}

    @Override
    protected void init() {
        super.init();
        layout = new LinearLayout(imageWidth, 1, LinearLayout.Orientation.VERTICAL);
        layout.defaultCellSetting().alignHorizontallyCenter().padding(10);
        layout.addChild(new StringWidget(Component.translatable(titleTranslationKey), this.font));
        this.dmxAddress = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("fixture.dmxStart"));
        this.dmxAddress.setValue(Integer.toString(this.be.getChannelStart()));
        layout.addChild(dmxAddress);
        this.dmxUniverse = new LabeledEditBox(this.font, xCenter, yCenter, 50, 10, Component.translatable("artneti.dmxUniverse"));
        this.dmxUniverse.setValue(Integer.toString(this.be.getUniverse()));
        layout.addChild(dmxUniverse);
        addExtraWidgetsToUI();
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
            new UpdateDMXFixture(blockPos, dmx, universe).sendToServer();
            new UpdateNetworkId(blockPos, networkId).sendToServer();
        } catch(NumberFormatException ignored) {
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

    private void renderWindow(GuiGraphics guiGraphics){
        int layoutHeight = 0;
        if(layout != null) {
            layoutHeight = layout.getHeight();
        }
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - layoutHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, imageWidth, layoutHeight, 0, 0, this.imageWidth, this.imageHeight, 256,256);
    }

    protected void renderLabels(GuiGraphics guiGraphics) {
//        renderLabel(guiGraphics, titleTranslationKey, 5,5);
//        renderLabel(guiGraphics, "fixture.dmxStart", 0,15);
//        renderLabel(guiGraphics, "artneti.dmxUniverse", 0,40);
//        renderLabel(guiGraphics, "artneti.network", 0,60);
    }

    protected void renderLabel(GuiGraphics guiGraphics, String translationKey, int offSetX, int offSetY){
        MutableComponent translatable = Component.translatable(translationKey);
        guiGraphics.drawString(font, translatable, xCenter + (this.imageWidth / 2) - (this.font.width(translatable.getString()) / 2), yCenter + offSetY, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
