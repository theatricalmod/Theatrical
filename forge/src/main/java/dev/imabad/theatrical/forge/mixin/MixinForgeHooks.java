package dev.imabad.theatrical.forge.mixin;

import dev.imabad.theatrical.util.ExtServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public class MixinForgeHooks {

    @Inject(method = "onBlockBreakEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/GameType;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 1),
    cancellable = true)
    private static void onBlockBreak(Level level, GameType gameType, ServerPlayer entityPlayer, BlockPos pos, CallbackInfoReturnable<Integer> cir){
        if(((ExtServerPlayerGameMode) entityPlayer.gameMode).shouldCaptureSentBlockEntities()){
            ((ExtServerPlayerGameMode) entityPlayer.gameMode).setCapturedBlockEntity(true);
            cir.setReturnValue(-1);
            cir.cancel();
        }
    }

}
