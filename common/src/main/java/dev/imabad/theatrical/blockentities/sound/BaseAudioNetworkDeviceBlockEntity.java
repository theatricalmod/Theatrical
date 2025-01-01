package dev.imabad.theatrical.blockentities.sound;

import dev.imabad.theatrical.api.network.audio.AudioNetworkDevice;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.networks.AVNetworkData;
import dev.imabad.theatrical.util.DimensionBlockPos;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public abstract class BaseAudioNetworkDeviceBlockEntity extends ClientSyncBlockEntity implements AudioNetworkDevice {
    private UUID networkId = UUIDUtil.NULL;

    public BaseAudioNetworkDeviceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }


    @Override
    public void write(CompoundTag compoundTag) {
        compoundTag.putUUID("network", networkId);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        networkId = compoundTag.getUUID("network");
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide) {
            addToNetwork();
        }
    }

    @Override
    public void setRemoved() {
        if(level != null && !level.isClientSide) {
            removeFromNetwork();
        }
        super.setRemoved();
    }

    @Override
    public UUID getNetworkId() {
        return networkId;
    }

    @Override
    public void setNetworkId(UUID newNetworkId) {
        if(newNetworkId == networkId){
            return;
        }
        removeFromNetwork();
        networkId = newNetworkId;
        addToNetwork();
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void removeFromNetwork(){
        var avNetwork = AVNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (avNetwork != null) {
            avNetwork.getAudioHandler().removeDevice(DimensionBlockPos.of(level.dimension(), getBlockPos()));
        }
    }

    private void addToNetwork(){
        var avNetwork = AVNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
        if (avNetwork != null) {
            avNetwork.getAudioHandler().addDevice(DimensionBlockPos.of(level.dimension(), getBlockPos()), this);
        }
    }
}
