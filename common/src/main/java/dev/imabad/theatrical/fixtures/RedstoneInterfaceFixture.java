package dev.imabad.theatrical.fixtures;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.HangType;
import dev.imabad.theatrical.api.dmx.DMXPersonality;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

// This isn't a real fixture, but it requires a fixture object now...
public class RedstoneInterfaceFixture extends Fixture {

    private static final List<DMXPersonality> PERSONALITIES = Collections.singletonList(
            new DMXPersonality(1, "1-Channel Mode")
                    .addSlot(SharedSlots.INTENSITY)
    );

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
        return null;
    }

    @Override
    public float[] getTiltRotationPosition() {
        return new float[0];
    }

    @Override
    public float[] getPanRotationPosition() {
        return new float[0];
    }

    @Override
    public float[] getBeamStartPosition() {
        return new float[0];
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
        return null;
    }

    @Override
    public float[] getTransforms(BlockState fixtureBlockState, BlockState supportBlockState) {
        return new float[0];
    }

    @Override
    public List<DMXPersonality> getDMXPersonalities() {
        return PERSONALITIES;
    }
}
