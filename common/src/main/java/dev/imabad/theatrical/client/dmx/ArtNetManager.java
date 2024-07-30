package dev.imabad.theatrical.client.dmx;

import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArtNetManager {

    private static final UUID NULL = UUIDUtil.NULL;

    private TheatricalArtNetClient artNetClient;
    private final Map<UUID, String> knownNetworks = new HashMap<>();

    private UUID networkId = NULL;

    public Map<UUID, String> getKnownNetworks() {
        return knownNetworks;
    }

    public void populateNetworks(Map<UUID, String> networks){
        knownNetworks.clear();
        knownNetworks.putAll(networks);
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
        if(artNetClient != null){
            artNetClient.networkChange();
        }
        ServerData currentServer = Minecraft.getInstance().getCurrentServer();
        if(currentServer != null){
            SavedClientNetworkManager.getInstance().saveNetworkForIP(currentServer.ip, networkId);
        } else {
            if(Minecraft.getInstance().getSingleplayerServer() != null) {
                ArtNetToNetworkClientData instance = ArtNetToNetworkClientData.getInstance(Minecraft.getInstance().getSingleplayerServer().overworld());
                instance.setNetworkId(networkId);
            }
        }
    }

    public UUID getNetworkId(){
        return this.networkId;
    }

    public UUID getSavedNetworkID(){
        ServerData currentServer = Minecraft.getInstance().getCurrentServer();
        if(currentServer != null){
            UUID networkFromIP = SavedClientNetworkManager.getInstance().getNetworkFromIP(currentServer.ip);
            if(networkFromIP != null) {
                return networkFromIP;
            }
        } else {
            if(Minecraft.getInstance().getSingleplayerServer() != null) {
                ArtNetToNetworkClientData instance = ArtNetToNetworkClientData.getInstance(Minecraft.getInstance().getSingleplayerServer().overworld());
                return instance.getNetworkId();
            }
        }
        return NULL;
    }

    public TheatricalArtNetClient getClient(){
        if(this.artNetClient == null){
            this.artNetClient = newClient();
        }
        return this.artNetClient;
    }

    private TheatricalArtNetClient newClient(){
        try {
            InetAddress byName = InetAddress.getByName(TheatricalConfig.INSTANCE.CLIENT.artNetIP);
            networkId = getSavedNetworkID();
            TheatricalArtNetClient client = new TheatricalArtNetClient(byName, this);
            client.start(byName);
            return client;
        } catch (UnknownHostException var3) {
            var3.printStackTrace();
        }
        return null;
    }

    public void shutdownAll(){
        knownNetworks.clear();
        if(artNetClient == null){
            return;
        }
        artNetClient.stop();
        artNetClient = null;
    }
}
