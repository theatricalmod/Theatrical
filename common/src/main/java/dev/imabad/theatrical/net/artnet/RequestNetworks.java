package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.*;
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
            List<DMXNetwork> networksForPlayer = DMXNetworkData.getInstance(level)
                    .getNetworksForPlayer(context.getPlayer().getUUID());
            Map<UUID, String> collect = networksForPlayer.stream().collect(Collectors.toMap(DMXNetwork::id, DMXNetwork::name));
            new NotifyNetworks(collect).sendTo((ServerPlayer) context.getPlayer());
        }
    }
}
