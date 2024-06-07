package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseDMXConsumerLightBlockEntity extends BaseLightBlockEntity implements DMXConsumer {

    private int channelCount, channelStartPoint;

    public BaseDMXConsumerLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
        compoundTag.putInt("channelCount", channelCount);
        compoundTag.putInt("channelStartPoint", channelStartPoint);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        channelCount = compoundTag.getInt("channelCount");
        channelStartPoint = compoundTag.getInt("channelStartPoint");
    }

    @Override
    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public int getChannelStart() {
        return channelStartPoint;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public void setChannelStartPoint(int channelStartPoint) {
        this.channelStartPoint = channelStartPoint;
        this.setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide) {
            var dmxData = DMXNetworkData.getInstance();
            if (dmxData != null) {
                dmxData.addConsumer(getBlockPos(), this);
            }
        }
    }

    @Override
    public void setRemoved() {
        if(level != null && !level.isClientSide) {
            var dmxData = DMXNetworkData.getInstance();
            if (dmxData != null) {
                dmxData.removeConsumer(getBlockPos());
            }
        }
        super.setRemoved();
    }
}
