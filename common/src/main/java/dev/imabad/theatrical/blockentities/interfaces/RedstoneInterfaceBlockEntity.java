package dev.imabad.theatrical.blockentities.interfaces;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.util.RndUtils;
import dev.imabad.theatrical.util.TheatricalCodecs;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class RedstoneInterfaceBlockEntity extends ClientSyncBlockEntity implements DMXConsumer {

    private int channelStartPoint, dmxUniverse = 0;
    private int redstoneOutput = 0;
    private RDMDeviceId deviceId;
    private UUID networkId = UUIDUtil.NULL;

    public RedstoneInterfaceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.REDSTONE_INTERFACE.get(), blockPos, blockState);
    }

    @Override
    public void write(ValueOutput output) {
        output.putInt("channelCount", 1);
        output.putInt("channelStartPoint", channelStartPoint);
        output.putInt("dmxUniverse", dmxUniverse);
        if(deviceId != null) {
            output.store("deviceId", TheatricalCodecs.BYTE_ARRAY, deviceId.toBytes());
        }
        output.store("network", net.minecraft.core.UUIDUtil.CODEC, networkId);
    }

    @Override
    public void read(ValueInput input) {
        channelStartPoint = input.getIntOr("channelStartPoint", 0);
        dmxUniverse = input.getIntOr("dmxUniverse", 0);
        input.read("deviceId", TheatricalCodecs.BYTE_ARRAY).ifPresent(bytes -> {
            deviceId = new RDMDeviceId(bytes);
        });
        input.read("network", net.minecraft.core.UUIDUtil.CODEC).ifPresent(inID -> {
            networkId = inID;
        });
    }

    @Override
    public int getChannelCount() {
        return 1;
    }

    @Override
    public int getChannelStart() {
        return channelStartPoint;
    }

    @Override
    public int getUniverse() {
        return dmxUniverse;
    }

    public int getRedstoneOutput() {
        return redstoneOutput;
    }

    @Override
    public RDMDeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public int getDeviceTypeId() {
        return 0x04;
    }

    @Override
    public String getModelName() {
        return "Redstone Interface";
    }

    @Override
    public Identifier getFixtureId() {
        return Fixtures.REDSTONE_INTERFACE.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    @Override
    public UUID getNetworkId() {
        return networkId;
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

    private void generateDeviceId(){
        byte[] bytes = new byte[4];
        if(level != null) {
            RndUtils.nextBytes(level.getRandom(), bytes);
        } else {
            new Random().nextBytes(bytes);
        }
        deviceId = new RDMDeviceId(Constants.MANUFACTURER_ID, bytes);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void consume(byte[] dmxValues) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ this.getChannelCount());
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
        this.setChanged();
        updateConsumer();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setUniverse(int universe) {
        if(this.dmxUniverse == universe){
            return;
        }
        removeConsumer();
        this.dmxUniverse = universe;
        addConsumer();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void updateConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (network != null) {
            network.dmx().updateConsumer(this);
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

    private void removeConsumer(){
        var network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (network != null) {
            network.dmx().removeConsumer(this, getBlockPos());
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

    @Override
    public String getTranslationKey() {
        return "block.theatrical.redstone_interface";
    }

}
