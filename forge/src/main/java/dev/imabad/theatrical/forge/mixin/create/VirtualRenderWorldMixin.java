package dev.imabad.theatrical.forge.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(VirtualRenderWorld.class)
public class VirtualRenderWorldMixin {

    @ModifyExpressionValue(
            method = "removeBlockEntity",
            at=@At(value = "INVOKE", target="Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object injectRemoveBlockEntity(Object original){
        if(original instanceof BlockEntity be){
            be.setRemoved();
        }
        return original;
    }
}
