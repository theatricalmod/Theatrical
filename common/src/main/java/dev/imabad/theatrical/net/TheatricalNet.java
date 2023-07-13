package dev.imabad.theatrical.net;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.imabad.theatrical.Theatrical;

public interface TheatricalNet {
    SimpleNetworkManager MAIN = SimpleNetworkManager.create(Theatrical.MOD_ID);

    MessageType SEND_ARTNET_TO_SERVER = MAIN.registerC2S("send_artnet_to_server", SendArtNetData::new);
    MessageType UPDATE_ARTNET_INTERFACE = MAIN.registerC2S("update_artnet_interface", UpdateArtNetInterface::new);
    MessageType UPDATE_DMX_FIXTURE = MAIN.registerC2S("update_dmx_fixture", UpdateDMXFixture::new);
    MessageType SYNC_CABLE_NETWORK = MAIN.registerS2C("sync_cable_network", SyncCableNetwork::new);
    static void init(){}
}
