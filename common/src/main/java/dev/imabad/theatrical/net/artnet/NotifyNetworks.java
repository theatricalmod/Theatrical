package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record NotifyNetworks(Map<UUID, String> networks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NotifyNetworks> TYPE = new CustomPacketPayload.Type<>(Theatrical.location("notify_networks"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotifyNetworks> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    UUIDUtil.STREAM_CODEC,
                    ByteBufCodecs.STRING_UTF8
            ),
            NotifyNetworks::networks,
            NotifyNetworks::new
    );

    public void handle(NetworkManager.PacketContext context) {
        TheatricalClient.getArtNetManager().populateNetworks(networks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
