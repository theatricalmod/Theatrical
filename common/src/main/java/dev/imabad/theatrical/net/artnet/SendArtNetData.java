package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.UUID;

public record SendArtNetData(UUID networkId, int universe, byte[] artNetData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SendArtNetData> TYPE = new CustomPacketPayload.Type<>(Theatrical.location("artnet_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendArtNetData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            SendArtNetData::networkId,
            ByteBufCodecs.INT,
            SendArtNetData::universe,
            ByteBufCodecs.BYTE_ARRAY,
            SendArtNetData::artNetData,
            SendArtNetData::new
    );

    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null) {
            TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
            UUID uuid = context.getPlayer().getUUID();
            if(network != null) {
                if (network.members().isMember(uuid) && network.members().canSendDMX(uuid)) {
                    Collection<DMXConsumer> consumers = network.dmx().getConsumers(universe);
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
