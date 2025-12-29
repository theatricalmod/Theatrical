package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record RequestConsumers(UUID networkId, int universe) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestConsumers> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("request_consumers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestConsumers> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RequestConsumers::networkId,
            ByteBufCodecs.INT,
            RequestConsumers::universe,
            RequestConsumers::new
    );

    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
            if(network != null && network.members().isMember(context.getPlayer().getUUID())){
                List<DMXDevice> devices = new ArrayList<>();
                Collection<DMXConsumer> consumers = network.dmx().getConsumers(universe);
                if(consumers == null){
                    return;
                }
                consumers.forEach(consumer -> {
                    devices.add(new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(),
                            consumer.getChannelCount(), consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(),
                            consumer.getFixtureId()));
                });
                NetworkManager.sendToPlayer((ServerPlayer) context.getPlayer(),  new ListConsumers(universe, devices));
            }else {
                Theatrical.LOGGER.info("{} tried to request data about a network that does not exist", context.getPlayer().getName().getString());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
