package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.UUID;

public class SendArtNetData extends BaseC2SMessage {

    private final UUID networkId;
    private final int universe;
    private final byte[] artNetData;

    public SendArtNetData(UUID networkId, int universe, byte[] data){
        this.networkId = networkId;
        this.universe = universe;
        artNetData = data;
    }

    public SendArtNetData(FriendlyByteBuf buf){
        networkId = buf.readUUID();
        universe = buf.readInt();
        artNetData = buf.readByteArray();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SEND_ARTNET_TO_SERVER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(networkId);
        buf.writeInt(universe);
        buf.writeByteArray(artNetData);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null) {
            TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
            UUID uuid = context.getPlayer().getUUID();
            if(network != null) {
                if (network.members().isMember(uuid) && network.members().canSendDMX(uuid)) {
                    Collection<DMXConsumer> consumers = network.dmx().getConsumers(universe);
                    network.setDmxData(universe, artNetData);
                    if(consumers != null) {
                        consumers.forEach(consumer -> {
                            consumer.consume(artNetData);
                        });
                    }
                } else {
                    Theatrical.LOGGER.info("{} tried to send ArtNet data to a network ({}) that they don't have permissions for", context.getPlayer().getName().getString(), network.name());
                }
            } else {
                Theatrical.LOGGER.info("{} tried to send ArtNet data to a network that doesn't exist.", context.getPlayer().getName().getString());
            }
        }
    }
}
