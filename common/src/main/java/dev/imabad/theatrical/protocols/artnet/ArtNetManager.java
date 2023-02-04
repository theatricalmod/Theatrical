package dev.imabad.theatrical.protocols.artnet;

import ch.bildspur.artnet.ArtNetClient;

import java.util.HashMap;

public class ArtNetManager {

    private final HashMap<String, ArtNetClient> clients = new HashMap<>();

    public ArtNetClient getClient(String ip){
        if(!this.clients.containsKey(ip)){
            return this.newClient(ip);
        }
        return this.clients.get(ip);
    }

    private ArtNetClient newClient(String ip){
        ArtNetClient client = new ArtNetClient();
        clients.put(ip, client);
        client.start(ip);
        return client;
    }

    public void shutdownAll(){
        clients.values().forEach(ArtNetClient::stop);
        clients.clear();
    }

}
