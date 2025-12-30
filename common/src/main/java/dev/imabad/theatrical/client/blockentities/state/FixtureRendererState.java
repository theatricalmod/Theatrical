package dev.imabad.theatrical.client.blockentities.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class FixtureRendererState extends BlockEntityRenderState {

    public boolean isFlipped;
    public boolean isHanging;
    public Direction facing;

    public float intensity;
    public float interpolatedIntensity;
    public int colour;
    public int focus;
    public float distance;
    public int pan, prevPan, tilt, prevTilt;
    public float interpolatedPan, interpolatedTilt;

    public FixtureRenderState fixtureRenderState = new FixtureRenderState();
    public Direction hangDirection;
    public float[] supportTransforms;

}
