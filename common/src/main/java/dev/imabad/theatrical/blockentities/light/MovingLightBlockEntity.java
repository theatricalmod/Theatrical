package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class MovingLightBlockEntity extends BaseDMXConsumerLightBlockEntity {
    public MovingLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        setChannelCount(7);
    }

    public MovingLightBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.MOVING_LIGHT.get(), pos, state);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.MOVING_LIGHT.get();
    }

    @Override
    public void consume(byte[] dmxValues) {
        byte[] ourValues = Arrays.copyOfRange(dmxValues, this.getChannelStart(),
                this.getChannelStart() + this.getChannelCount());
        if(ourValues.length < 7){
            return;
        }
        if(this.storePrev()){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        intensity = convertByteToInt(ourValues[0]);
        red = convertByteToInt(ourValues[1]);
        green = convertByteToInt(ourValues[2]);
        blue = convertByteToInt(ourValues[3]);
        focus = convertByteToInt(ourValues[4]);
        pan = (int) ((convertByteToInt(ourValues[5]) * 360) / 255f);
        tilt = (int) ((convertByteToInt(ourValues[6]) * 180) / 255F);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }
    @Override
    public boolean isUpsideDown() {
        return getBlockState().getValue(MovingLightBlock.HANGING);
    }
}
