package dev.imabad.theatrical.net.compat.create;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class SendBEDataToContraption extends BaseS2CMessage {

    private int entityId;
    private BlockPos posInContraption;
    private CompoundTag blockEntityData;

    public SendBEDataToContraption(int entityId, BlockPos posInContraption, CompoundTag blockEntityData) {
        this.entityId = entityId;
        this.posInContraption = posInContraption;
        this.blockEntityData = blockEntityData;
    }

    public SendBEDataToContraption(FriendlyByteBuf friendlyByteBuf){
        entityId = friendlyByteBuf.readInt();
        posInContraption = friendlyByteBuf.readBlockPos();
        blockEntityData = friendlyByteBuf.readAnySizeNbt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SEND_BE_DATA_TO_CONTRAPTION;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBlockPos(posInContraption);
        buf.writeNbt(blockEntityData);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            TheatricalExpectPlatform.handleBEDataForContraption(this);
        });
    }

    public int getEntityId() {
        return entityId;
    }

    public BlockPos getPosInContraption() {
        return posInContraption;
    }

    public CompoundTag getBlockEntityData() {
        return blockEntityData;
    }
}
