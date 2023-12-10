package dev.imabad.theatrical.blockentities.light;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightCollisionContext implements CollisionContext {

    private BlockPos fromPos;

    public LightCollisionContext(BlockPos fromPos) {
        this.fromPos = fromPos;
    }

    public BlockPos getFromPos() {
        return fromPos;
    }

    @Override
    public boolean isDescending() {
        return false;
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean canAscend) {
        return false;
    }

    @Override
    public boolean isHoldingItem(Item item) {
        return false;
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2) {
        return false;
    }

}
