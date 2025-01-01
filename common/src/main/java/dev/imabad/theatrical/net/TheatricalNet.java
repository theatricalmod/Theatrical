package dev.imabad.theatrical.net;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.net.artnet.*;
import dev.imabad.theatrical.net.sound.*;

public interface TheatricalNet {
    SimpleNetworkManager MAIN = SimpleNetworkManager.create(Theatrical.MOD_ID);
    // C2S
    MessageType SEND_ARTNET_TO_SERVER = MAIN.registerC2S("send_artnet_to_server", SendArtNetData::new);
    MessageType UPDATE_ARTNET_INTERFACE = MAIN.registerC2S("update_artnet_interface", UpdateArtNetInterface::new);
    MessageType UPDATE_DMX_FIXTURE = MAIN.registerC2S("update_dmx_fixture", UpdateDMXFixture::new);
    MessageType UPDATE_FIXTURE_POS = MAIN.registerC2S("update_fixture_pos", UpdateFixturePosition::new);
    MessageType RDM_UPDATE_FIXTURE = MAIN.registerC2S("rdm_update_fixture", RDMUpdateConsumer::new);
    MessageType REQUEST_CONSUMERS = MAIN.registerC2S("request_consumers", RequestConsumers::new);
    MessageType UPDATE_CONSOLE_FADER = MAIN.registerC2S("update_console_fader", ControlUpdateFader::new);
    MessageType CONTROL_MOVE_STEP = MAIN.registerC2S("control_move_step", ControlMoveStep::new);
    MessageType CONTROL_MODE_TOGGLE = MAIN.registerC2S("control_mode_toggle", ControlModeToggle::new);
    MessageType CONTROL_GO = MAIN.registerC2S("control_go", ControlGo::new);
    MessageType REQUEST_NETWORKS = MAIN.registerC2S("request_networks", RequestNetworks::new);
    MessageType UPDATE_NETWORK_ID = MAIN.registerC2S("update_network_id", UpdateNetworkId::new);
    MessageType CONFIGURE_CONFIGURATION_CARD = MAIN.registerC2S("configure_configuration_card", ConfigureConfigurationCard::new);

    // S2C
    MessageType NOTIFY_CONSUMER_CHANGE = MAIN.registerS2C("notify_consumer_change", NotifyConsumerChange::new);
    MessageType LIST_CONSUMERS = MAIN.registerS2C("list_consumers", ListConsumers::new);
    MessageType NOTIFY_NETWORKS = MAIN.registerS2C("notify_networks", NotifyNetworks::new);
    MessageType OPEN_SCREEN = MAIN.registerS2C("open_screen", OpenScreen::new);

    MessageType SPEAKER_AUDIO = MAIN.registerS2C("speaker_audio_client", SpeakerAudioClient::new);
    MessageType START_MIC = MAIN.registerS2C("start_mic", StartMicrophone::new);
    MessageType PLAYER_AUDIO = MAIN.registerC2S("player_audio", MicrophoneAudioPacket::new);
    MessageType CONFIGURE_MIXER = MAIN.registerC2S("configure_mixer", ConfigureMixerPacket::new);
    MessageType SET_MIXER_VOLUME = MAIN.registerC2S("set_mixer_volume", SetMixerVolumePacket::new);
    static void init(){}
}
