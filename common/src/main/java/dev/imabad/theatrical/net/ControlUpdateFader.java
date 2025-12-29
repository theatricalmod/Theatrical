package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ControlUpdateFader(BlockPos pos, int fader, int value) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ControlUpdateFader> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("control_update_fader"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ControlUpdateFader> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ControlUpdateFader::pos,
            ByteBufCodecs.INT,
            ControlUpdateFader::fader,
            ByteBufCodecs.INT,
            ControlUpdateFader::value,
            ControlUpdateFader::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(pos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            lightingDeskBlock.setFader(fader, value);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
