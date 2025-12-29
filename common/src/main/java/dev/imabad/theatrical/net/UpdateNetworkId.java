package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.BelongsToNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public record UpdateNetworkId(BlockPos blockPos, UUID networkId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateNetworkId> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("update_network_id"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateNetworkId> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateNetworkId::blockPos,
            UUIDUtil.STREAM_CODEC,
            UpdateNetworkId::networkId,
            UpdateNetworkId::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BelongsToNetwork belongsToNetwork){
            belongsToNetwork.setNetworkId(networkId);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
