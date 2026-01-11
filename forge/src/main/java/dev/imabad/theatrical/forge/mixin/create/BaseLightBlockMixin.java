package dev.imabad.theatrical.forge.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseLightBlock.class, remap = false)
public abstract class BaseLightBlockMixin {


    @ModifyReturnValue(method = "Ldev/imabad/theatrical/blocks/light/BaseLightBlock;getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
    at = @At("RETURN"), remap = false)
    public VoxelShape injectGetShape(VoxelShape original, @Local(argsOnly = true) BlockState state,  @Local(argsOnly = true)  BlockGetter level,  @Local(argsOnly = true)  BlockPos pos, @Local(argsOnly = true)  CollisionContext context) {
        if(level instanceof ContraptionWorld && pos.equals(BlockPos.ZERO.below())) {
            return Shapes.empty();
        }
        return original;
    }
}
