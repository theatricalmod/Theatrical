package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record RequestNetworks() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestNetworks> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("request_networks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestNetworks> STREAM_CODEC = StreamCodec.unit(new RequestNetworks());

    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            List<TheatricalNetwork> networksForPlayer = TheatricalNetworkData.getInstance(level.getServer().overworld())
                    .getNetworksForPlayer(context.getPlayer().getUUID());
            Map<UUID, String> collect = networksForPlayer.stream().collect(Collectors.toMap(TheatricalNetwork::id, TheatricalNetwork::name));
            NetworkManager.sendToPlayer((ServerPlayer) context.getPlayer(), new NotifyNetworks(collect));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
