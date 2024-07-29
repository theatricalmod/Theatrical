package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpdateArtNetInterface extends BaseC2SMessage {

    private BlockPos pos;
    private String ipAddress;
    private int dmxUniverse;

    public UpdateArtNetInterface(BlockPos blockPos, String ipAddress, int dmxUniverse){
        this.pos = blockPos;
        this.ipAddress = ipAddress;
        this.dmxUniverse = dmxUniverse;
    }

    UpdateArtNetInterface(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        dmxUniverse = buf.readInt();
        ipAddress = buf.readUtf();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.UPDATE_ARTNET_INTERFACE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(dmxUniverse);
        buf.writeUtf(ipAddress);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BlockEntity be = context.getPlayer().level().getBlockEntity(pos);
        if(be instanceof ArtNetInterfaceBlockEntity artNetInterfaceBlockEntity){
//            if(artNetInterfaceBlockEntity.getOwnerUUID().equals(context.getPlayer().getUUID())){
//                artNetInterfaceBlockEntity.updateConfig(ipAddress, dmxUniverse);
//            }
        }
    }
}
