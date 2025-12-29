package dev.imabad.theatrical.net.artnet;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public record RDMUpdateConsumer(UUID networkId, int universe, int newAddress, RDMDeviceId dmxDevice) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RDMUpdateConsumer> TYPE =
            new CustomPacketPayload.Type<>(Theatrical.location("rdm_update_consumer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RDMUpdateConsumer> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            RDMUpdateConsumer::networkId,
            ByteBufCodecs.INT,
            RDMUpdateConsumer::universe,
            ByteBufCodecs.INT,
            RDMUpdateConsumer::newAddress,
            DMXDevice.RDM_DEVICE_ID_CODEC,
            RDMUpdateConsumer::dmxDevice,
            RDMUpdateConsumer::new
    );

    public void handle(NetworkManager.PacketContext context) {
        Level level = context.getPlayer().level();
        if(level.getServer() != null ) {
            TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
            if(network == null || !network.members().canSendDMX(context.getPlayer().getUUID())) {
                Theatrical.LOGGER.info("{} tried to send an RDM update for a network that doesn't exist or isn't part of", context.getPlayer().getName().getString());
                return;
            }
            BlockPos consumerPos = network.dmx().getConsumerPos(universe, dmxDevice);
            if(consumerPos != null){
                BlockEntity be = context.getPlayer().level().getBlockEntity(consumerPos);
                if(be instanceof BaseDMXConsumerLightBlockEntity dmxConsumerLightBlock){
                    dmxConsumerLightBlock.setChannelStartPoint(newAddress);
                } else if(be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity){
                    redstoneInterfaceBlockEntity.setChannelStartPoint(newAddress);
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
