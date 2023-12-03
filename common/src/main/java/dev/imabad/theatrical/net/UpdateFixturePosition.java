package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateFixturePosition extends BaseC2SMessage {

    private BlockPos pos;
    private int tilt,pan;

    public UpdateFixturePosition(BlockPos blockPos, int tilt, int pan){
        this.pos = blockPos;
        this.tilt = tilt;
        this.pan = pan;
    }

    UpdateFixturePosition(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        tilt = buf.readInt();
        pan = buf.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_FIXTURE_POS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(tilt);
        buf.writeInt(pan);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(pos);
        if(be instanceof BaseLightBlockEntity baseLightBlockEntity){
            baseLightBlockEntity.setPan(pan);
            baseLightBlockEntity.setTilt(tilt);
        }
    }
}
