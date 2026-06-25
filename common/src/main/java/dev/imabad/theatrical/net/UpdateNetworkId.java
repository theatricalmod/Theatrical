package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.BelongsToNetwork;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.util.DmxPacketGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class UpdateNetworkId extends BaseC2SMessage {

    private final BlockPos blockPos;
    private final UUID networkId;

    public UpdateNetworkId(BlockPos blockPos, UUID networkId) {
        this.blockPos = blockPos;
        this.networkId = networkId;
    }

    UpdateNetworkId(FriendlyByteBuf friendlyByteBuf){
        blockPos = friendlyByteBuf.readBlockPos();
        networkId = friendlyByteBuf.readUUID();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_NETWORK_ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeUUID(networkId);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (!DmxPacketGuard.canReach(player, blockPos)) {
            return;
        }
        TheatricalNetwork network = DmxPacketGuard.resolveNetwork((ServerLevel) player.level(), networkId);
        if (network == null || !DmxPacketGuard.canConfigure(player, network)) {
            Theatrical.LOGGER.info("{} tried to assign a block to a network they cannot access", player.getName().getString());
            return;
        }
        BlockEntity be = player.level().getBlockEntity(blockPos);
        if(be instanceof BelongsToNetwork belongsToNetwork){
            belongsToNetwork.setNetworkId(networkId);
        }
    }
}
