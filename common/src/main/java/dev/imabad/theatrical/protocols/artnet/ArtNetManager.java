package dev.imabad.theatrical.protocols.artnet;

import ch.bildspur.artnet.ArtNetClient;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.dmx.TheatricalArtNetClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ArtNetManager {

    private TheatricalArtNetClient artNetClient;

    public TheatricalArtNetClient getClient(){
        if(this.artNetClient == null){
            this.artNetClient = newClient();
        }
        return this.artNetClient;
    }

    private TheatricalArtNetClient newClient(){
        try {
            InetAddress byName = InetAddress.getByName(TheatricalConfig.INSTANCE.CLIENT.artNetIP);
            TheatricalArtNetClient client = new TheatricalArtNetClient(byName);
            client.start(byName);
            return client;
        } catch (UnknownHostException var3) {
            var3.printStackTrace();
        }
        return null;
    }

    public void shutdownAll(){
        if(artNetClient == null){
            return;
        }
        artNetClient.stop();
        artNetClient = null;
    }
}
