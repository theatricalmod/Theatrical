package dev.imabad.theatrical.blockentities.light;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.util.RndUtils;
import dev.imabad.theatrical.util.TheatricalCodecs;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void write(ValueOutput out) {
        super.write(out);
        out.putInt("channelCount", channelCount);
        out.putInt("channelStartPoint", channelStartPoint);
        out.putInt("dmxUniverse", dmxUniverse);
        if(deviceId != null) {
            out.store("deviceId", TheatricalCodecs.BYTE_ARRAY, deviceId.toBytes());
        }
        out.store("network", net.minecraft.core.UUIDUtil.CODEC, networkId);
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        channelCount = input.getIntOr("channelCount", 0);
        channelStartPoint = input.getIntOr("channelStartPoint", 0);
        dmxUniverse = input.getIntOr("dmxUniverse", 0);
        input.read("deviceId", TheatricalCodecs.BYTE_ARRAY).ifPresent(bytes -> {
            deviceId = new RDMDeviceId(bytes);
        });
        input.read("network", net.minecraft.core.UUIDUtil.CODEC).ifPresent(inID -> {
            networkId = inID;
        });
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
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (network != null) {
            network.dmx().updateConsumer(this);
        }
    }
    private void removeConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (network != null) {
            network.dmx().removeConsumer(this, getBlockPos());
        }
    }
    private void addConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (network != null) {
            if(deviceId == null){
                generateDeviceId();
            }
            network.dmx().addConsumer(getBlockPos(), this);
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide()) {
            addConsumer();
        }
    }

    @Override
    public void setRemoved() {
        if(level != null && !level.isClientSide()) {
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
