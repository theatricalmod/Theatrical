package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateDMXFixture extends BaseC2SMessage {

    private BlockPos pos;
    private int dmxAddress, dmxUniverse;

    public UpdateDMXFixture(BlockPos blockPos, int dmxAddress, int dmxUniverse){
        this.pos = blockPos;
        this.dmxAddress = dmxAddress;
        this.dmxUniverse = dmxUniverse;
    }

    UpdateDMXFixture(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        dmxAddress = buf.readInt();
        dmxUniverse = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_DMX_FIXTURE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(dmxAddress);
        buf.writeInt(dmxUniverse);
    }

    @Override
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
}
