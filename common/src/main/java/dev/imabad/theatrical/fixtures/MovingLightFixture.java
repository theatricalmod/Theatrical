package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.api.dmx.DMXPersonality;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class MovingLightFixture extends Fixture {

    private static final List<DMXPersonality> PERSONALITIES = Collections.singletonList(
            new DMXPersonality(7, "7-Channel Mode")
                    .addSlot(SharedSlots.INTENSITY)
                    .addSlot(SharedSlots.RED)
                    .addSlot(SharedSlots.GREEN)
                    .addSlot(SharedSlots.BLUE)
                    .addSlot(SharedSlots.FOCUS)
                    .addSlot(SharedSlots.PAN)
                    .addSlot(SharedSlots.TILT)
    );

    private static final ResourceLocation TILT_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_tilt");
    private static final ResourceLocation PAN_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_pan");
    private static final ResourceLocation STATIC_MODEL = new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_static");

    private final float[] tiltRotation = new float[]{0.5F, .5F, .5F};
    private final float[] panRotation = new float[]{0.5F, .5F, .5F};
//    private final float[] beamStartPosition = new float[]{0.5F, -0.8F, 0F};


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

    @Override
    public float[] getTransforms(BlockState fixtureBlockState, BlockState supportBlockState) {
        if(fixtureBlockState.getValue(BaseLightBlock.HANG_DIRECTION) == Direction.UP){
            return new float[]{0, .5f, 0};
        }
        return new float[]{0, -0.35F, 0};
    }

    @Override
    public List<DMXPersonality> getDMXPersonalities() {
        return PERSONALITIES;
    }

    @Override
    public boolean isUpsideDown(BlockState blockState) {
        return blockState.getValue(MovingLightBlock.HANGING) && blockState.getValue(MovingLightBlock.HANG_DIRECTION) == Direction.UP;
    }
}
