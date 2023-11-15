package dev.imabad.theatrical.mixin;

import dev.imabad.theatrical.util.ExtServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl{

    @Shadow public ServerPlayer player;
    @Shadow private int ackBlockChangesUpTo;
    @Shadow public abstract void send(Packet<?> packet);

    @Inject(method = "handlePlayerAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;)V",
            at = @At(value = "INVOKE", target="Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;II)V",
                    shift = At.Shift.BEFORE))
    public void beforeSendBreak(ServerboundPlayerActionPacket packet, CallbackInfo ci){
        ((ExtServerPlayerGameMode) player.gameMode).setCapturedBlockEntity(false);
        ((ExtServerPlayerGameMode) player.gameMode).setCaptureSentBlockEntities(true);
    }


    @Inject(method = "handlePlayerAction(Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;)V",
            at = @At(value = "INVOKE", target="Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V",
                    shift = At.Shift.AFTER))
    public void onStopDestroyAck(ServerboundPlayerActionPacket packet, CallbackInfo ci){
        ((ExtServerPlayerGameMode) player.gameMode).setCaptureSentBlockEntities(false);
        if(((ExtServerPlayerGameMode) player.gameMode).hasCapturedBlockEntity()){
            BlockPos blockPos = packet.getPos();
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.player.connection.ackBlockChangesUpTo = -1;

            ((ExtServerPlayerGameMode) player.gameMode).setCapturedBlockEntity(false);
            // Update any tile entity data for this block
            BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
            if (blockEntity != null)
            {
                Packet<?> pkt = blockEntity.getUpdatePacket();
                if (pkt != null)
                {
                    this.player.connection.send(pkt);
                }
            }
        }

    }

}
