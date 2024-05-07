package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateConsoleFader extends BaseC2SMessage {

    private final BlockPos blockPos;
    private final int fader;
    private final int value;

    public UpdateConsoleFader(BlockPos blockPos, int fader, int value) {
        this.blockPos = blockPos;
        this.fader = fader;
        this.value = value;
    }

    UpdateConsoleFader(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.fader = buf.readInt();
        this.value = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_CONSOLE_FADER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(fader);
        buf.writeInt(value);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            lightingDeskBlock.setFader(fader, value);
        }
    }
}
