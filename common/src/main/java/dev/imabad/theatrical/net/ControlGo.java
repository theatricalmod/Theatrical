package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ControlGo extends BaseC2SMessage {

    private final BlockPos blockPos;
    private final int fadeInTicks, fadeOutTicks;

    public ControlGo(BlockPos blockPos, int fadeInTicks, int fadeOutTicks) {
        this.blockPos = blockPos;
        this.fadeInTicks = fadeInTicks;
        this.fadeOutTicks = fadeOutTicks;
    }

    ControlGo(FriendlyByteBuf buf){
        this.blockPos = buf.readBlockPos();
        this.fadeInTicks = buf.readInt();
        this.fadeOutTicks = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.CONTROL_GO;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(fadeInTicks);
        buf.writeInt(fadeOutTicks);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            if(!lightingDeskBlock.isRunMode()){
                lightingDeskBlock.setFadeInTicks(fadeInTicks);
                lightingDeskBlock.setFadeOutTicks(fadeOutTicks);
            }
            lightingDeskBlock.clickButton();
        }
    }
}
