package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LEDFresnelFixture extends Fixture {

    private static final ResourceLocation TILT_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_body_only");
    private static final ResourceLocation PAN_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_handle_only");
    private static final ResourceLocation STATIC_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_hook_bar");

    private final float[] tiltRotation = new float[]{0.5F, .3F, .39F};
    private final float[] panRotation = new float[]{0.5F, 0F, .41F};
    private final float[] beamStartPosition = new float[]{0.5F, 0.24F, 0.1F};

    @Override
    public ResourceLocation getTiltModel() {
        return TILT_MODEL;
    }

    @Override
    public ResourceLocation getPanModel() {
        return PAN_MODEL;
    }

    @Override
    public ResourceLocation getStaticModel() {
        return STATIC_MODEL;
    }

    @Override
    public float[] getTiltRotationPosition() {
        return tiltRotation;
    }

    @Override
    public float[] getPanRotationPosition() {
        return panRotation;
    }

    @Override
    public float[] getBeamStartPosition() {
        return beamStartPosition;
    }

    @Override
    public float getDefaultRotation() {
        return 0;
    }

    @Override
    public float getBeamWidth() {
        return 0.15f;
    }

    @Override
    public float getRayTraceRotation() {
        return 180f;
    }

    @Override
    public HangType getHangType() {
        return HangType.HOOK_BAR;
    }

    @Override
    public float[] getTransforms(BlockState fixtureBlockState, BlockState supportBlockState) {
        if(fixtureBlockState.getValue(BaseLightBlock.HANG_DIRECTION) == Direction.UP){
            return new float[]{0, .5f, 0};
        }
        return new float[]{0, 0.5F, 0};
    }
}
