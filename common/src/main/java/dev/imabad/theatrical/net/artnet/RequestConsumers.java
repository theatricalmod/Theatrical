package dev.imabad.theatrical.net.artnet;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class RequestConsumers extends BaseC2SMessage {

    private final UUID networkId;
    private final int universe;

    public RequestConsumers(UUID networkId, int universe){
        this.networkId = networkId;
        this.universe = universe;
    }

    public RequestConsumers(FriendlyByteBuf byteBuf){
        networkId = byteBuf.readUUID();
        universe = byteBuf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.REQUEST_CONSUMERS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(networkId);
        buf.writeInt(universe);
    }

    @Override
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
                new ListConsumers(universe, devices).sendTo((ServerPlayer)context.getPlayer());
            }else {
                Theatrical.LOGGER.info("{} tried to request data about a network that does not exist", context.getPlayer().getName().getString());
            }
        }
    }
}
