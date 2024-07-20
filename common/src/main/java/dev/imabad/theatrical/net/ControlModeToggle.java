package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ControlModeToggle extends BaseC2SMessage {

    private final BlockPos blockPos;

    public ControlModeToggle(BlockPos pos){
        this.blockPos = pos;
    }

    ControlModeToggle(FriendlyByteBuf buf){
        this.blockPos = buf.readBlockPos();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.CONTROL_MODE_TOGGLE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            lightingDeskBlock.toggleMode();
        }
    }
}
