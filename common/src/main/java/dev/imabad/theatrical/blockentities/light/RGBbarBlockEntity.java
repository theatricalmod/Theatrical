package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class RGBbarBlockEntity extends BaseDMXConsumerLightBlockEntity {

    public RGBbarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.RGB_BAR.get(), pos, state);
        setChannelCount(4);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.RGB_BAR.get();
    }

    @Override
    public int getFocus() {
        return 1;
    }

    @Override
    public void consume(byte[] dmxValues) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ this.getChannelCount());
        if(ourValues.length < 4){
            return;
        }
        if(this.storePrev()){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        intensity = convertByteToInt(ourValues[0]);
        red = convertByteToInt(ourValues[1]);
        green = convertByteToInt(ourValues[2]);
        blue = convertByteToInt(ourValues[3]);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        setChanged();
    }

    @Override
    public int getDeviceTypeId() {
        return 0x02;
    }

    @Override
    public String getModelName() {
        return "RGB Bar";
    }

    @Override
    public ResourceLocation getFixtureId() {
        return Fixtures.RGB_BAR.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    @Override
    public float getMaxLightDistance() {
        return 1;
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

}