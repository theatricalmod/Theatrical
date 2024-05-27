package dev.imabad.theatrical.net.artnet;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RDMUpdateConsumer extends BaseC2SMessage {

    private int universe, newAddress;;
    private RDMDeviceId dmxDevice;

    public RDMUpdateConsumer(int universe, RDMDeviceId dmxDevice, int newAddress){
        this.universe = universe;
        this.dmxDevice = dmxDevice;
        this.newAddress = newAddress;
    }

    public RDMUpdateConsumer(FriendlyByteBuf buf){
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
        buf.writeInt(universe);
        buf.writeByteArray(dmxDevice.toBytes());
        buf.writeInt(newAddress);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            if (context.getPlayer().hasPermissions(level.getServer().getOperatorUserPermissionLevel())) {
                BlockPos consumerPos = DMXNetworkData.getInstance().getConsumerPos(universe, dmxDevice);
                if(consumerPos != null){
                    BlockEntity be = context.getPlayer().level().getBlockEntity(consumerPos);
                    if(be instanceof BaseDMXConsumerLightBlockEntity dmxConsumerLightBlock){
                        dmxConsumerLightBlock.setChannelStartPoint(newAddress);
                    } else if(be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity){
                        redstoneInterfaceBlockEntity.setChannelStartPoint(newAddress);
                    }
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
