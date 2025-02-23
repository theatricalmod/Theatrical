package dev.imabad.theatrical.client.gui.screen;

import dev.imabad.theatrical.blockentities.light.BigPanel2BlockEntity;
import dev.imabad.theatrical.client.gui.widgets.BasicSlider;
import dev.imabad.theatrical.net.UpdateFixturePosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

public class BigPanel2Screen extends GenericDMXConfigurationScreen<BigPanel2BlockEntity> {
    private BasicSlider tiltSlider, panSlider;
    private final BigPanel2BlockEntity be;

    public BigPanel2Screen(BigPanel2BlockEntity be) {
        super(be, be.getBlockPos(), "block.theatrical.big_panel2");
        this.be = be;
    }

    @Override
    public void addExtraWidgetsToUI() {
        this.tiltSlider = new BasicSlider(xCenter + 13, yCenter + 45, 150, 20, Component.empty(), be.getTilt(), -90, 90, (newTilt) -> {
            be.setTilt(newTilt.intValue());
        });
        this.panSlider = new BasicSlider(xCenter, yCenter + 75, 150, 20, Component.empty(), be.getPan(),-180, 180, (newPan) -> {
            be.setPan(newPan.intValue());
        });
        LayoutSettings layoutSettings = layout.newChildLayoutSettings().paddingVertical(2);
        layout.addChild(new StringWidget(Component.translatable("fixture.tilt"), font), layoutSettings);
        layout.addChild(tiltSlider, layoutSettings);
        layout.addChild(new StringWidget(Component.translatable("fixture.pan"), font), layoutSettings);
        layout.addChild(panSlider, layoutSettings);
    }

    @Override
    protected void update() {
        super.update();
        new UpdateFixturePosition(be.getBlockPos(), be.getTilt(), be.getPan()).sendToServer();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics) {
        super.renderLabels(guiGraphics);
//        renderLabel(guiGraphics, "fixture.tilt", 0, 36);
//        renderLabel(guiGraphics, "fixture.pan", 0, 66);
    }
}