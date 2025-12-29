package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

public record UpdateFixturePosition(BlockPos pos, int tilt, int pan) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateFixturePosition> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("update_fixture_position"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFixturePosition> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateFixturePosition::pos,
            ByteBufCodecs.INT,
            UpdateFixturePosition::tilt,
            ByteBufCodecs.INT,
            UpdateFixturePosition::pan,
            UpdateFixturePosition::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(pos);
        if(be instanceof BaseLightBlockEntity baseLightBlockEntity){
            baseLightBlockEntity.setPan(pan);
            baseLightBlockEntity.setTilt(tilt);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
