package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.dmx.DMXDevice;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public record NotifyConsumerChange(int universe, ChangeType changeType, DMXDevice dmxDevice) implements CustomPacketPayload {
    public static final Type<NotifyConsumerChange> TYPE = new Type<>(Theatrical.location("notify_consumer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NotifyConsumerChange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            NotifyConsumerChange::universe,
            ChangeType.ID_STREAM_CODEC,
            NotifyConsumerChange::changeType,
            DMXDevice.STREAM_CODEC,
            NotifyConsumerChange::dmxDevice,
            NotifyConsumerChange::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum ChangeType {
        ADD,
        UPDATE,
        REMOVE;


        public static final IntFunction<ChangeType> BY_ID =
                ByIdMap.continuous(
                        ChangeType::ordinal,
                        ChangeType.values(),
                        ByIdMap.OutOfBoundsStrategy.ZERO
                );

        public static final StreamCodec<ByteBuf, ChangeType> ID_STREAM_CODEC =
                ByteBufCodecs.idMapper(ChangeType.BY_ID, ChangeType::ordinal);
    }


//    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getEnv() == EnvType.CLIENT) {
            TheatricalClient.handleConsumerChange(this);
        }
    }
}
