package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SendArtNetData extends BaseC2SMessage {
    private BlockPos pos;
    private byte[] artNetData;

    public SendArtNetData(BlockPos blockPos, byte[] data){
        pos = blockPos;
        artNetData = data;
    }

    SendArtNetData(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        artNetData = buf.readByteArray();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SEND_ARTNET_TO_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByteArray(artNetData);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level;
        BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof ArtNetInterfaceBlockEntity artnetInterface) {
            if(level.getServer() != null && context.getPlayer().hasPermissions(level.getServer().getOperatorUserPermissionLevel())){
                artnetInterface.update(artNetData);
            }
        }
    }
}
