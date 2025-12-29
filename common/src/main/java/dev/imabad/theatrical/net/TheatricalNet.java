package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.imabad.theatrical.net.artnet.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface TheatricalNet {

    static void init(){
        // S2C
        registerS2C(ListConsumers.TYPE, ListConsumers.STREAM_CODEC, ListConsumers::handle);
        registerS2C(NotifyNetworks.TYPE, NotifyNetworks.STREAM_CODEC, NotifyNetworks::handle);
        registerS2C(OpenScreen.TYPE, OpenScreen.STREAM_CODEC, OpenScreen::handle);

        // C2S
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SendArtNetData.TYPE, SendArtNetData.STREAM_CODEC, SendArtNetData::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, UpdateDMXFixture.TYPE, UpdateDMXFixture.STREAM_CODEC, UpdateDMXFixture::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, UpdateFixturePosition.TYPE, UpdateFixturePosition.STREAM_CODEC, UpdateFixturePosition::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RDMUpdateConsumer.TYPE, RDMUpdateConsumer.STREAM_CODEC, RDMUpdateConsumer::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RequestConsumers.TYPE, RequestConsumers.STREAM_CODEC, RequestConsumers::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ControlUpdateFader.TYPE, ControlUpdateFader.STREAM_CODEC, ControlUpdateFader::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ControlMoveStep.TYPE, ControlMoveStep.STREAM_CODEC, ControlMoveStep::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ControlModeToggle.TYPE, ControlModeToggle.STREAM_CODEC, ControlModeToggle::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ControlGo.TYPE, ControlGo.STREAM_CODEC, ControlGo::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, RequestNetworks.TYPE, RequestNetworks.STREAM_CODEC, RequestNetworks::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, UpdateNetworkId.TYPE, UpdateNetworkId.STREAM_CODEC, UpdateNetworkId::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ConfigureConfigurationCard.TYPE, ConfigureConfigurationCard.STREAM_CODEC, ConfigureConfigurationCard::handle);
    }

    private static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> packetType,
                                                                    StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
                                                                    NetworkManager.NetworkReceiver<T> receiver) {
        if (Platform.getEnvironment().equals(Env.SERVER)) {
            NetworkManager.registerS2CPayloadType(packetType, codec);
        } else {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, packetType, codec, receiver);
        }
    }
}
