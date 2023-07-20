package dev.imabad.theatrical.protocols.artnet;

import ch.bildspur.artnet.ArtNetClient;
import dev.imabad.theatrical.dmx.TheatricalArtNetClient;

import java.util.HashMap;

public class ArtNetManager {

    private final HashMap<String, TheatricalArtNetClient> clients = new HashMap<>();

    public TheatricalArtNetClient getClient(String ip){
        if(!this.clients.containsKey(ip)){
            return this.newClient(ip);
        }
        return this.clients.get(ip);
    }

    private TheatricalArtNetClient newClient(String ip){
        TheatricalArtNetClient client = new TheatricalArtNetClient();
        clients.put(ip, client);
        client.start(ip);
        return client;
    }

    public void shutdownAll(){
        clients.values().forEach(ArtNetClient::stop);
        clients.clear();
    }

}
