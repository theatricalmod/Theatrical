package dev.imabad.theatrical.graphs;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.api.CableType;

import java.util.UUID;

public abstract class CableNetworkPacket extends BaseS2CMessage {

    public UUID networkId;
    public CableType networkType;
    public boolean packetDeletesNetwork;

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> handle(TheatricalClient.CABLES, TheatricalClient.CABLES.getOrCreateNetwork(networkId, networkType)));
    }

    protected abstract void handle(GlobalCableManager manager, CableNetwork network);
}
