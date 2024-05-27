package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SendArtNetData extends BaseC2SMessage {
    private int universe;
    private byte[] artNetData;

    public SendArtNetData(int universe, byte[] data){
        this.universe = universe;
        artNetData = data;
    }

    public SendArtNetData(FriendlyByteBuf buf){
        universe = buf.readInt();
        artNetData = buf.readByteArray();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SEND_ARTNET_TO_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(universe);
        buf.writeByteArray(artNetData);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            if (context.getPlayer().hasPermissions(level.getServer().getOperatorUserPermissionLevel())) {
                DMXNetworkData.getInstance().getConsumers(universe).forEach(consumer -> {
                    consumer.consume(artNetData);
                });
                DMXNetworkData.getInstance().addKnownSender((ServerPlayer) context.getPlayer());
            } else {
                Theatrical.LOGGER.info("{} tried to send ArtNet data but is not authorized!", context.getPlayer().getName());
            }
        }
    }
}
