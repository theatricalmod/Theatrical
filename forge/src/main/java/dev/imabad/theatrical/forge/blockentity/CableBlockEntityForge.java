package dev.imabad.theatrical.forge.blockentity;

import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.CableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

public class CableBlockEntityForge extends CableBlockEntity {

    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
    public static final ModelProperty<boolean[]> SIDES = new ModelProperty<>();
    public static final ModelProperty<CableType> TYPE = new ModelProperty<>();
    public static final ModelProperty<ResourceKey<Level>> DIMENSION = new ModelProperty<>();

    public CableBlockEntityForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(POS, getBlockPos()).with(SIDES, cableSides)
                .with(TYPE, getBlockState().getValue(CableBlock.CABLE_TYPE))
                .with(DIMENSION, getLevel().dimension()).build();
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        requestModelDataUpdate();
    }

    @Override
    protected void triggerUpdates() {
        super.triggerUpdates();
        requestModelDataUpdate();
    }

    @Override
    public void neighboursUpdated() {
        super.neighboursUpdated();
        setChanged();
        requestModelDataUpdate();
    }


}
