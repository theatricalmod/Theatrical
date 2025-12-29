package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;

public record UpdateDMXFixture(BlockPos pos, int dmxAddress, int dmxUniverse) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateDMXFixture> TYPE =
            new CustomPacketPayload.Type<>(Theatrical.location("update_dmx_fixture"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDMXFixture> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateDMXFixture::pos,
            ByteBufCodecs.INT,
            UpdateDMXFixture::dmxAddress,
            ByteBufCodecs.INT,
            UpdateDMXFixture::dmxUniverse,
            UpdateDMXFixture::new
    );

    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(pos);
        if(be instanceof BaseDMXConsumerLightBlockEntity dmxConsumerLightBlock){
            dmxConsumerLightBlock.setChannelStartPoint(dmxAddress);
            dmxConsumerLightBlock.setUniverse(dmxUniverse);
        } else if(be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity){
            redstoneInterfaceBlockEntity.setChannelStartPoint(dmxAddress);
            redstoneInterfaceBlockEntity.setUniverse(dmxUniverse);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
