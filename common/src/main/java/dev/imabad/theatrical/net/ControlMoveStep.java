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

public record ControlMoveStep(BlockPos blockPos, boolean forward) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ControlMoveStep> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("control_move_step"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ControlMoveStep> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ControlMoveStep::blockPos,
            ByteBufCodecs.BOOL,
            ControlMoveStep::forward,
            ControlMoveStep::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            if(forward) {
                lightingDeskBlock.moveForward();
            } else {
                lightingDeskBlock.moveBack();
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
