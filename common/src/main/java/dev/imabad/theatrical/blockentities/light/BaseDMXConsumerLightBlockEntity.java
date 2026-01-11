package dev.imabad.theatrical.blockentities.light;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.api.dmx.BaseDMXData;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.util.RndUtils;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.UUID;

public abstract class BaseDMXConsumerLightBlockEntity extends BaseLightBlockEntity implements DMXConsumer {

    private BaseDMXData dmxData = new BaseDMXData();

    public BaseDMXConsumerLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
        if(dmxData != null) {
            DataResult<Tag> encode = BaseDMXData.CODEC.encodeStart(NbtOps.INSTANCE, dmxData);
            Tag dmxDataTag = encode.result().orElse(new CompoundTag());
            compoundTag.put("dmxData", dmxDataTag);
        }
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        this.dmxData = BaseDMXData.fromCompoundTag(compoundTag);
        if(compoundTag.contains("last_data")) {
            consume(compoundTag.getByteArray("last_data"), true);
        }
    }

    private void generateDeviceId(){
        byte[] bytes = new byte[4];
        if(level != null) {
            RndUtils.nextBytes(level.getRandom(), bytes);
        } else {
            new Random().nextBytes(bytes);
        }
        dmxData.setDeviceId(new RDMDeviceId(Constants.MANUFACTURER_ID, bytes));
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public int getChannelCount() {
        return dmxData.getChannelCount();
    }

    @Override
    public int getChannelStart() {
        return dmxData.getChannelStart();
    }

    @Override
    public int getUniverse() {
        return dmxData.getUniverse();
    }

    @Override
    public RDMDeviceId getDeviceId() {
        return dmxData.getDeviceId();
    }

    public UUID getNetworkId() {
        return dmxData.getNetworkId();
    }

    public void setUniverse(int dmxUniverse) {
        if(this.dmxData.getUniverse() == dmxUniverse){
            return;
        }
        removeConsumer();
        dmxData.setUniverse(dmxUniverse);
        addConsumer();
        notifyChanged();
    }

    public void setChannelCount(int channelCount) {
        dmxData.setChannelCount(channelCount);
    }

    public void setChannelStartPoint(int channelStartPoint) {
        if(dmxData.getChannelStart() == channelStartPoint){
            return;
        }
        dmxData.setChannelStart(channelStartPoint);
        updateConsumer();
        notifyChanged();
    }

    @Override
    public void setStartAddress(int startAddress) {
        setChannelStartPoint(startAddress);
    }

    private void updateConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(dmxData.getNetworkId());
        if (network != null) {
            network.dmx().updateConsumer(this);
        }
    }
    private void removeConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(dmxData.getNetworkId());
        if (network != null) {
            network.dmx().removeConsumer(this);
        }
    }
    private void addConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(dmxData.getNetworkId());
        if (network != null) {
            if(dmxData.getDeviceId() == null){
                generateDeviceId();
            }
            network.dmx().addConsumer(this);
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
        if(networkId == this.getNetworkId()){
            return;
        }
        removeConsumer();
        dmxData.setNetworkId(networkId);
        addConsumer();
        notifyChanged();
    }

}
