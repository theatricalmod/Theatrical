package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.dmx.DMXDevice;
import net.fabricmc.api.EnvType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record ListConsumers(int universe, List<DMXDevice> dmxDevices) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ListConsumers> TYPE = new CustomPacketPayload.Type<>(Theatrical.location("list_consumers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ListConsumers> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ListConsumers::universe,
            DMXDevice.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ListConsumers::dmxDevices,
            ListConsumers::new
    );

    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnv() == EnvType.CLIENT){
            TheatricalClient.handleListConsumers(this);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
