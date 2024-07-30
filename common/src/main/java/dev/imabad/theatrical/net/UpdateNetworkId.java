package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.api.dmx.BelongsToNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class UpdateNetworkId extends BaseC2SMessage {

    private BlockPos blockPos;
    private UUID networkId;

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
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BelongsToNetwork belongsToNetwork){
            belongsToNetwork.setNetworkId(networkId);
        }
    }
}
