package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import net.minecraft.resources.ResourceLocation;

public class MovingLightFixture extends Fixture {

    private final float[] tiltRotation = new float[]{0.5F, .5F, .5F};
    private final float[] panRotation = new float[]{0.5F, .5F, .5F};
//    private final float[] beamStartPosition = new float[]{0.5F, -0.8F, 0F};


    @Override
    public ResourceLocation getTiltModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_tilt");
    }

    @Override
    public ResourceLocation getPanModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_pan");
    }

    @Override
    public ResourceLocation getStaticModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_static");
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
        return new float[]{0.5F, 0.5F, 0.2F};
    }

    @Override
    public float getDefaultRotation() {
        return 90;
    }

    @Override
    public float getBeamWidth() {
        return 0.15f;
    }

    @Override
    public float getRayTraceRotation() {
        return 0;
    }

    @Override
    public HangType getHangType() {
        return HangType.BRACE_BAR;
    }
}
