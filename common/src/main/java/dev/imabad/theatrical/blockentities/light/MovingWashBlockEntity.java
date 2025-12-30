package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.MovingWashBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class MovingWashBlockEntity extends BaseDMXConsumerLightBlockEntity {
    public MovingWashBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        setChannelCount(7);
    }

    public MovingWashBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.MOVING_WASH.get(), pos, state);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.MOVING_WASH.get();
    }

    @Override
    public void consume(byte[] dmxValues) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ this.getChannelCount());
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
        pan = (int) ((convertByteToInt(ourValues[5]) * 360) / 255f) - 180;
        tilt = (int) ((convertByteToInt(ourValues[6]) * 270) / 255F) - 225;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        setChanged();
    }

    @Override
    public int getDeviceTypeId() {
        return 0x01;
    }

    @Override
    public String getModelName() {
        return "Moving Wash";
    }

    @Override
    public Identifier getFixtureId() {
        return Fixtures.MOVING_WASH.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }
    @Override
    public boolean isUpsideDown() {
        return getBlockState().getValue(MovingWashBlock.HANGING) && getBlockState().getValue(MovingWashBlock.HANG_DIRECTION) == Direction.UP;
    }

    @Override
    public int getBasePan() {
        return 0;
    }

    @Override
    public String getTranslationKey() {
        return "block.theatrical.moving_wash";
    }
}
