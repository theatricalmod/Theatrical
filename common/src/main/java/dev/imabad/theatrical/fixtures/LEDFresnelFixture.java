package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.api.dmx.DMXPersonality;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class LEDFresnelFixture extends Fixture {

    private static final List<DMXPersonality> PERSONALITIES = Collections.singletonList(
            new DMXPersonality(4, "4-Channel Mode")
                    .addSlot(SharedSlots.INTENSITY)
                    .addSlot(SharedSlots.RED)
                    .addSlot(SharedSlots.GREEN)
                    .addSlot(SharedSlots.BLUE)
    );

    private static final Identifier TILT_MODEL = Theatrical.location( "block/fresnel/fresnel_body_only");
    private static final Identifier PAN_MODEL = Theatrical.location( "block/fresnel/fresnel_handle_only");
    private static final Identifier STATIC_MODEL = Theatrical.location( "block/fresnel/fresnel_hook_bar");

    private final float[] tiltRotation = new float[]{0.5F, .3F, .39F};
    private final float[] panRotation = new float[]{0.5F, 0F, .41F};
    private final float[] beamStartPosition = new float[]{0.5F, 0.24F, 0.1F};

    @Override
    public Identifier getTiltModel() {
        return TILT_MODEL;
    }

    @Override
    public Identifier getPanModel() {
        return PAN_MODEL;
    }

    @Override
    public Identifier getStaticModel() {
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

    @Override
    public List<DMXPersonality> getDMXPersonalities() {
        return PERSONALITIES;
    }

    @Override
    public boolean invertTilt() {
        return true;
    }

    @Override
    public boolean invertPan() {
        return true;
    }

    @Override
    public double getLightRadius() {
        return 2.0;
    }
}
