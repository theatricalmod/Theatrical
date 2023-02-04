package dev.imabad.theatrical.api;

import net.minecraft.resources.ResourceLocation;

public abstract class Fixture {
    public abstract ResourceLocation getTiltModel();

    public abstract ResourceLocation getPanModel();

    public abstract ResourceLocation getStaticModel();

    public abstract float[] getTiltRotationPosition();

    public abstract float[] getPanRotationPosition();

    public abstract float[] getBeamStartPosition();

    public abstract float getDefaultRotation();

    public abstract float getBeamWidth();

    public abstract float getRayTraceRotation();

    public abstract HangType getHangType();
}
