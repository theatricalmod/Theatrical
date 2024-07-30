package dev.imabad.theatrical.blockentities.light;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.util.RndUtils;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public abstract class BaseDMXConsumerLightBlockEntity extends BaseLightBlockEntity implements DMXConsumer {

    private int channelCount, channelStartPoint, dmxUniverse;
    private RDMDeviceId deviceId;
    private UUID networkId = UUIDUtil.NULL;

    public BaseDMXConsumerLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
        compoundTag.putInt("channelCount", channelCount);
        compoundTag.putInt("channelStartPoint", channelStartPoint);
        compoundTag.putInt("dmxUniverse", dmxUniverse);
        if(deviceId != null) {
            compoundTag.putByteArray("deviceId", deviceId.toBytes());
        }
        compoundTag.putUUID("network", networkId);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        channelCount = compoundTag.getInt("channelCount");
        channelStartPoint = compoundTag.getInt("channelStartPoint");
        if(compoundTag.contains("dmxUniverse")){
            dmxUniverse = compoundTag.getInt("dmxUniverse");
        }
        if(compoundTag.contains("deviceId")){
            deviceId = new RDMDeviceId(compoundTag.getByteArray("deviceId"));
        }
        if(compoundTag.contains("network")){
            networkId = compoundTag.getUUID("network");
        }
    }

    private void generateDeviceId(){
        byte[] bytes = new byte[4];
        if(level != null) {
            RndUtils.nextBytes(level.getRandom(), bytes);
        } else {
            new Random().nextBytes(bytes);
        }
        deviceId = new RDMDeviceId(Constants.MANUFACTURER_ID, bytes);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public int getChannelCount() {
        return channelCount;
    }

    @Override
    public int getChannelStart() {
        return channelStartPoint;
    }

    @Override
    public int getUniverse() {
        return dmxUniverse;
    }

    @Override
    public RDMDeviceId getDeviceId() {
        return deviceId;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public void setUniverse(int dmxUniverse) {
        if(this.dmxUniverse == dmxUniverse){
            return;
        }
        removeConsumer();
        this.dmxUniverse = dmxUniverse;
        addConsumer();
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public void setChannelStartPoint(int channelStartPoint) {
        if(this.channelStartPoint == channelStartPoint){
            return;
        }
        this.channelStartPoint = channelStartPoint;
        updateConsumer();
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    private void updateConsumer(){
        var dmxData = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (dmxData != null) {
            dmxData.updateConsumer(this);
        }
    }
    private void removeConsumer(){
        var dmxData = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (dmxData != null) {
            dmxData.removeConsumer(this, getBlockPos());
        }
    }
    private void addConsumer(){
        var dmxData = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (dmxData != null) {
            if(deviceId == null){
                generateDeviceId();
            }
            dmxData.addConsumer(getBlockPos(), this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide) {
            addConsumer();
        }
    }

    @Override
    public void setRemoved() {
        if(level != null && !level.isClientSide) {
            removeConsumer();
        }
        super.setRemoved();
    }

    public void setNetworkId(UUID networkId) {
        if(networkId == this.networkId){
            return;
        }
        removeConsumer();
        this.networkId = networkId;
        addConsumer();
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
