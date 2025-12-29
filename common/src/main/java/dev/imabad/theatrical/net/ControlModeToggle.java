package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ControlModeToggle(BlockPos blockPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ControlModeToggle> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("control_mode_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ControlModeToggle> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ControlModeToggle::blockPos,
            ControlModeToggle::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            lightingDeskBlock.toggleMode();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
