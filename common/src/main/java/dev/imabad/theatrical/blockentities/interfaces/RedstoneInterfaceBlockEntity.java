package dev.imabad.theatrical.blockentities.interfaces;

import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class RedstoneInterfaceBlockEntity extends ClientSyncBlockEntity implements DMXConsumer {

    private int channelStartPoint = 0;
    private int redstoneOutput = 0;

    public RedstoneInterfaceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.REDSTONE_INTERFACE.get(), blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        compoundTag.putInt("channelCount", 1);
        compoundTag.putInt("channelStartPoint", channelStartPoint);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        channelStartPoint = compoundTag.getInt("channelStartPoint");
    }

    @Override
    public int getChannelCount() {
        return 1;
    }

    @Override
    public int getChannelStart() {
        return channelStartPoint;
    }

    public int getRedstoneOutput() {
        return redstoneOutput;
    }

    @Override
    public void consume(byte[] dmxValues) {
        byte[] ourValues = Arrays.copyOfRange(dmxValues, this.getChannelStart(),
                this.getChannelStart() + this.getChannelCount());
        if(ourValues.length < 1){
            return;
        }
        int newOutput = (int) Math.round(Mth.map((double)convertByteToInt(ourValues[0]), 0, 255, 0, 15));
        if(newOutput != redstoneOutput) {
            redstoneOutput = newOutput;
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    }
    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

    public void setChannelStartPoint(int channelStartPoint) {
        this.channelStartPoint = channelStartPoint;
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
