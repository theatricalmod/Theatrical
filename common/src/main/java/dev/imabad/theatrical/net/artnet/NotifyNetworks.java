package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.dmx.ArtNetManager;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotifyNetworks extends BaseS2CMessage {

    private final Map<UUID, String> networks;

    public NotifyNetworks(Map<UUID, String> networks) {
        this.networks = networks;
    }

    public NotifyNetworks(FriendlyByteBuf buf) {
        networks = new HashMap<>();
        int count = buf.readInt();
        for(int i = 0; i < count; i++){
            String name = buf.readUtf();
            UUID uuid = buf.readUUID();
            networks.put(uuid, name);
        }
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.NOTIFY_NETWORKS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(networks.size());
        for (UUID u : networks.keySet()) {
            buf.writeUtf(networks.get(u));
            buf.writeUUID(u);
        }
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        TheatricalClient.getArtNetManager().populateNetworks(networks);
    }
}
