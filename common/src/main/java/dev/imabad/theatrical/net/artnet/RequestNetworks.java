package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.networks.AVNetwork;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.networks.AVNetworkData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RequestNetworks extends BaseC2SMessage {

    public RequestNetworks() {
    }

    public RequestNetworks(FriendlyByteBuf buf){}

    @Override
    public MessageType getType() {
        return TheatricalNet.REQUEST_NETWORKS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            List<AVNetwork> networksForPlayer = AVNetworkData.getInstance(level.getServer().overworld())
                    .getNetworksForPlayer(context.getPlayer().getUUID());
            Map<UUID, String> collect = networksForPlayer.stream().collect(Collectors.toMap(AVNetwork::id, AVNetwork::name));
            new NotifyNetworks(collect).sendTo((ServerPlayer) context.getPlayer());
        }
    }
}
