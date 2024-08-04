package dev.imabad.theatrical.net.artnet;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class RDMUpdateConsumer extends BaseC2SMessage {

    private final UUID networkId;
    private final int universe;
    private final int newAddress;
    private final RDMDeviceId dmxDevice;

    public RDMUpdateConsumer(UUID networkId, int universe, RDMDeviceId dmxDevice, int newAddress){
        this.networkId = networkId;
        this.universe = universe;
        this.dmxDevice = dmxDevice;
        this.newAddress = newAddress;
    }

    public RDMUpdateConsumer(FriendlyByteBuf buf){
        networkId = buf.readUUID();
        universe = buf.readInt();
        dmxDevice = new RDMDeviceId(buf.readByteArray(6));
        newAddress = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.RDM_UPDATE_FIXTURE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(networkId);
        buf.writeInt(universe);
        buf.writeByteArray(dmxDevice.toBytes());
        buf.writeInt(newAddress);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            if (context.getPlayer().hasPermissions(level.getServer().getOperatorUserPermissionLevel())) {
                DMXNetwork network = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
                if(network == null){
                    return;
                }
                DMXConsumer consumer = network.getConsumer(universe, dmxDevice);
                if(consumer != null){
                    consumer.setStartAddress(newAddress);
                }
            } else {
                Theatrical.LOGGER.info("{} tried to send ArtNet data but is not authorized!", context.getPlayer().getName());
            }
        }
    }

    public int getUniverse() {
        return universe;
    }

    public int getNewAddress() {
        return newAddress;
    }
}
