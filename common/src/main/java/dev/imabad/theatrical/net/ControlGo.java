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

public record ControlGo(BlockPos blockPos, int fadeInTicks, int fadeOutTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ControlGo> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("control_go"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ControlGo> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ControlGo::blockPos,
            ByteBufCodecs.INT,
            ControlGo::fadeInTicks,
            ByteBufCodecs.INT,
            ControlGo::fadeOutTicks,
            ControlGo::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(blockPos);
        if(be instanceof BasicLightingDeskBlockEntity lightingDeskBlock){
            if(!lightingDeskBlock.isRunMode()){
                lightingDeskBlock.setFadeInTicks(fadeInTicks);
                lightingDeskBlock.setFadeOutTicks(fadeOutTicks);
            }
            lightingDeskBlock.clickButton();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
