package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.BelongsToNetwork;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.util.DmxPacketGuard;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateDMXFixture extends BaseC2SMessage {

    private final BlockPos pos;
    private final int dmxAddress;
    private final int dmxUniverse;

    public UpdateDMXFixture(BlockPos blockPos, int dmxAddress, int dmxUniverse){
        this.pos = blockPos;
        this.dmxAddress = dmxAddress;
        this.dmxUniverse = dmxUniverse;
    }

    UpdateDMXFixture(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        dmxAddress = buf.readInt();
        dmxUniverse = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_DMX_FIXTURE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(dmxAddress);
        buf.writeInt(dmxUniverse);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (!DmxPacketGuard.canReach(player, pos)) {
            return;
        }
        if (!DmxPacketGuard.isValidUniverse(dmxUniverse)) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof BaseDMXConsumerLightBlockEntity dmxConsumerLightBlock) {
            if (!canConfigureFixture(player, dmxConsumerLightBlock.getNetworkId())) {
                return;
            }
            if (!DmxPacketGuard.isValidAddress(dmxAddress)
                    || !DmxPacketGuard.fitsInUniverse(dmxAddress, dmxConsumerLightBlock.getChannelCount())) {
                return;
            }
            dmxConsumerLightBlock.setChannelStartPoint(dmxAddress);
            dmxConsumerLightBlock.setUniverse(dmxUniverse);
        } else if (be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity) {
            if (!canConfigureFixture(player, redstoneInterfaceBlockEntity.getNetworkId())) {
                return;
            }
            if (!DmxPacketGuard.isValidAddress(dmxAddress)
                    || !DmxPacketGuard.fitsInUniverse(dmxAddress, redstoneInterfaceBlockEntity.getChannelCount())) {
                return;
            }
            redstoneInterfaceBlockEntity.setChannelStartPoint(dmxAddress);
            redstoneInterfaceBlockEntity.setUniverse(dmxUniverse);
        }
    }

    private static boolean canConfigureFixture(ServerPlayer player, java.util.UUID networkId) {
        if (networkId.equals(UUIDUtil.NULL)) {
            return true;
        }
        TheatricalNetwork network = DmxPacketGuard.resolveNetwork((ServerLevel) player.level(), networkId);
        if (network == null || !DmxPacketGuard.canConfigure(player, network)) {
            Theatrical.LOGGER.info("{} tried to configure a fixture on a network they cannot access", player.getName().getString());
            return false;
        }
        return true;
    }
}
