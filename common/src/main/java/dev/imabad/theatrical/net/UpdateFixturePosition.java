package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.util.DmxPacketGuard;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateFixturePosition extends BaseC2SMessage {

    private final BlockPos pos;
    private final int tilt;
    private final int pan;

    public UpdateFixturePosition(BlockPos blockPos, int tilt, int pan){
        this.pos = blockPos;
        this.tilt = tilt;
        this.pan = pan;
    }

    UpdateFixturePosition(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        tilt = buf.readInt();
        pan = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_FIXTURE_POS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(tilt);
        buf.writeInt(pan);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (!DmxPacketGuard.canReach(player, pos)) {
            return;
        }
        BlockEntity be = player.level().getBlockEntity(pos);
        if(be instanceof BaseLightBlockEntity baseLightBlockEntity){
            if (be instanceof BaseDMXConsumerLightBlockEntity dmxConsumer) {
                if (!canConfigureFixture(player, dmxConsumer.getNetworkId())) {
                    return;
                }
            }
            baseLightBlockEntity.setPan(DmxPacketGuard.clampPanTilt(pan));
            baseLightBlockEntity.setTilt(DmxPacketGuard.clampPanTilt(tilt));
        }
    }

    private static boolean canConfigureFixture(ServerPlayer player, java.util.UUID networkId) {
        if (networkId.equals(UUIDUtil.NULL)) {
            return true;
        }
        TheatricalNetwork network = DmxPacketGuard.resolveNetwork((ServerLevel) player.level(), networkId);
        if (network == null || !DmxPacketGuard.canConfigure(player, network)) {
            Theatrical.LOGGER.info("{} tried to move a fixture on a network they cannot access", player.getName().getString());
            return false;
        }
        return true;
    }
}
