package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ControlUpdateFader extends BaseC2SMessage {

    private final BlockPos blockPos;
    private final int fader;
    private final float value;

    public ControlUpdateFader(BlockPos blockPos, int fader, float value) {
        this.blockPos = blockPos;
        this.fader = fader;
        this.value = value;
    }

    ControlUpdateFader(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.fader = buf.readInt();
        this.value = buf.readFloat();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_CONSOLE_FADER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(fader);
        buf.writeFloat(value);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            lightingDeskBlock.setFader(fader, Math.round(value));
        }
    }
}
