package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.blocks.rigging.PipeBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LEDPanelFixture extends Fixture {

    private static final ResourceLocation STATIC_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/led_panel");


    @Override
    public ResourceLocation getTiltModel() {
        return null;
    }

    @Override
    public ResourceLocation getPanModel() {
        return null;
    }

    @Override
    public ResourceLocation getStaticModel() {
        return STATIC_MODEL;
    }

    @Override
    public float[] getTiltRotationPosition() {
        return new float[3];
    }

    @Override
    public float[] getPanRotationPosition() {
        return new float[3];
    }

    @Override
    public float[] getBeamStartPosition() {
        return new float[3];
    }

    @Override
    public float getDefaultRotation() {
        return 0;
    }

    @Override
    public float getBeamWidth() {
        return 0;
    }

    @Override
    public float getRayTraceRotation() {
        return 0;
    }

    @Override
    public HangType getHangType() {
        return HangType.BRACE_BAR;
    }

    @Override
    public boolean hasBeam() {
        return false;
    }

    @Override
    public float[] getTransforms(BlockState fixtureBlockState, BlockState supportBlockState) {
        if(supportBlockState.getBlock() instanceof PipeBlock){
            return new float[]{0, 0, 1f};
        }
        return new float[]{0,0,0};
    }

}
