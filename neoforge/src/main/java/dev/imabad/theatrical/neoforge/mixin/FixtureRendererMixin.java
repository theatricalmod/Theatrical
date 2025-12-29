package dev.imabad.theatrical.neoforge.mixin;

import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.client.blockentities.FixtureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FixtureRenderer.class)
public class FixtureRendererMixin implements IBlockEntityRendererExtension<BaseLightBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(BaseLightBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos, blockEntity.getEmissionBlock());
    }
}
