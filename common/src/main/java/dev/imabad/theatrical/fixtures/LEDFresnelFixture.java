package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import net.minecraft.resources.ResourceLocation;

public class LEDFresnelFixture extends Fixture {
    private final float[] tiltRotation = new float[]{0.5F, .3F, .39F};
    private final float[] panRotation = new float[]{0.5F, 0F, .41F};
    private final float[] beamStartPosition = new float[]{0.5F, 0.24F, 0.1F};

    @Override
    public ResourceLocation getTiltModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_body_only");
    }

    @Override
    public ResourceLocation getPanModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_handle_only");
    }

    @Override
    public ResourceLocation getStaticModel() {
        return new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_hook_bar");
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
        return 0.25f;
    }

    @Override
    public float getRayTraceRotation() {
        return 0;
    }

    @Override
    public HangType getHangType() {
        return HangType.HOOK_BAR;
    }
}
