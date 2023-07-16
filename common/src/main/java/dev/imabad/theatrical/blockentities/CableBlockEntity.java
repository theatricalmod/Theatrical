package dev.imabad.theatrical.blockentities;

import dev.imabad.theatrical.graphs.GlobalCableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends ClientSyncBlockEntity {

    // 0 - down
    // 1 - up
    // 2 - north
    // 3 - south
    // 4 - west
    // 5 - east
    private boolean[] cableSides = new boolean[6];

    public CableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.CABLE.get(), blockPos, blockState);
    }

    private byte sidesToByte(){
        byte b = 0;
        for (int i = 0; i < 6; ++i) {
            if (cableSides[i]) b |= (1 << i);
        }
        return b;
    }

    private boolean[] sidesFromInt(byte b){
        boolean[] sides = new boolean[6];
        for (int i = 0; i < 6; ++i) {
            sides[i] = (b & (1 << i)) != 0;
        }
        return sides;
    }

    @Override
    public void write(CompoundTag compoundTag) {
        compoundTag.putByte("sides", sidesToByte());
    }

    @Override
    public void read(CompoundTag compoundTag) {
        cableSides = sidesFromInt(compoundTag.getByte("sides"));
    }

    public boolean isConnected(Direction direction){
        //TODO Complex logic!
        return false;
    }

    public boolean hasSide(Direction direction){
        return cableSides[direction.ordinal()];
    }

    public void removeSide(Direction direction){
        cableSides[direction.ordinal()] = false;
        if(!getLevel().isClientSide){
            GlobalCableManager.onCableSideRemoved(level, getBlockPos(), getBlockState(), direction);
        }
    }

    public void addSide(Direction direction){
        cableSides[direction.ordinal()] = true;
        if(!getLevel().isClientSide) {
            getLevel().scheduleTick(getBlockPos(), getBlockState().getBlock(), 1);
        }
    }

    public boolean hasActiveSide(){
        for(boolean b : cableSides){
            if(b) return true;
        }
        return false;
    }
}
