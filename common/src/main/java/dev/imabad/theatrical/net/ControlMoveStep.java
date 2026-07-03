package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.util.DmxPacketGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ControlMoveStep extends BaseC2SMessage {

    private final BlockPos blockPos;
    private final boolean forward;

    public ControlMoveStep(BlockPos pos, boolean forward) {
        this.blockPos = pos;
        this.forward = forward;
    }

    ControlMoveStep(FriendlyByteBuf buf){
        this.blockPos = buf.readBlockPos();
        this.forward = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.CONTROL_MOVE_STEP;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(forward);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        BlockEntity be = player.level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            if (!DmxPacketGuard.canControlConsole(player, lightingDeskBlock)) {
                return;
            }
            if(forward) {
                lightingDeskBlock.moveForward();
            } else {
                lightingDeskBlock.moveBack();
            }
        }
    }
}
