package dev.imabad.theatrical.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

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

    public boolean hasPanModel(){
        return getPanModel() != null;
    }
    public boolean hasTiltModel(){
        return getTiltModel() != null;
    }
    public boolean hasBeam(){
        return true;
    }
    public abstract float[] getTransforms(BlockState fixtureBlockState, BlockState supportBlockState);
}
