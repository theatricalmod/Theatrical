package dev.imabad.theatrical.client.gui.screen.fixtures;

import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.client.gui.screen.GenericManualPanTiltScreen;

public class FresnelScreen extends GenericManualPanTiltScreen {
    public FresnelScreen(FresnelBlockEntity be) {
        super(be, "block.theatrical.fresnel");
    }
}