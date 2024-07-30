package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.items.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ConfigureConfigurationCard extends BaseC2SMessage {

    private UUID network;
    private int dmxAddress, dmxUniverse;
    private boolean autoIncrement;

    public ConfigureConfigurationCard(UUID network, int dmxAddress, int dmxUniverse, boolean autoIncrement) {
        this.network = network;
        this.dmxAddress = dmxAddress;
        this.dmxUniverse = dmxUniverse;
        this.autoIncrement = autoIncrement;
    }

    public ConfigureConfigurationCard(FriendlyByteBuf buf){
        this.network = buf.readUUID();
        this.dmxAddress = buf.readInt();
        this.dmxUniverse = buf.readInt();
        this.autoIncrement = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.CONFIGURE_CONFIGURATION_CARD;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(network);
        buf.writeInt(dmxAddress);
        buf.writeInt(dmxUniverse);
        buf.writeBoolean(autoIncrement);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            ItemStack itemStack = null;
            if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CONFIGURATION_CARD.get()){
                itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            } else if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.CONFIGURATION_CARD.get()){
                itemStack = player.getItemInHand(InteractionHand.OFF_HAND);
            }
            if(itemStack != null){
                CompoundTag dataTag = itemStack.getOrCreateTag();
                dataTag.putUUID("network", network);
                dataTag.putInt("dmxUniverse", dmxUniverse);
                dataTag.putInt("dmxAddress", dmxAddress);
                dataTag.putBoolean("autoIncrement", autoIncrement);
                itemStack.save(dataTag);
            }
        });
    }
}
