package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.util.DmxPacketGuard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ConfigureConfigurationCard extends BaseC2SMessage {

    private final UUID network;
    private final int dmxAddress;
    private final int dmxUniverse;
    private final boolean autoIncrement;
    private final boolean universeEnabled;
    private final boolean addressEnabled;

    public ConfigureConfigurationCard(UUID network, int dmxAddress, int dmxUniverse, boolean autoIncrement,  boolean universeEnabled, boolean addressEnabled) {
        this.network = network;
        this.dmxAddress = dmxAddress;
        this.dmxUniverse = dmxUniverse;
        this.autoIncrement = autoIncrement;
        this.universeEnabled = universeEnabled;
        this.addressEnabled = addressEnabled;
    }

    public ConfigureConfigurationCard(FriendlyByteBuf buf){
        this.network = buf.readUUID();
        this.dmxAddress = buf.readInt();
        this.dmxUniverse = buf.readInt();
        this.autoIncrement = buf.readBoolean();
        this.universeEnabled = buf.readBoolean();
        this.addressEnabled = buf.readBoolean();
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
        buf.writeBoolean(universeEnabled);
        buf.writeBoolean(addressEnabled);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            TheatricalNetwork theatricalNetwork = DmxPacketGuard.resolveNetwork((ServerLevel) player.level(), network);
            if (theatricalNetwork == null || !DmxPacketGuard.canConfigure(player, theatricalNetwork)) {
                Theatrical.LOGGER.info("{} tried to configure a card for a network they cannot access", player.getName().getString());
                return;
            }
            if (addressEnabled && !DmxPacketGuard.isValidAddress(dmxAddress)) {
                return;
            }
            if (universeEnabled && !DmxPacketGuard.isValidUniverse(dmxUniverse)) {
                return;
            }
            ItemStack itemStack = findConfigurationCard(player);
            if(itemStack != null){
                CompoundTag dataTag = itemStack.getOrCreateTag();
                dataTag.putUUID("network", network);
                dataTag.putInt("dmxUniverse", dmxUniverse);
                dataTag.putInt("dmxAddress", dmxAddress);
                dataTag.putBoolean("autoIncrement", autoIncrement);
                dataTag.putBoolean("universeEnabled", universeEnabled);
                dataTag.putBoolean("addressEnabled", addressEnabled);
                itemStack.save(dataTag);
            }
        });
    }

    private static ItemStack findConfigurationCard(Player player) {
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.CONFIGURATION_CARD.get()){
            return player.getItemInHand(InteractionHand.MAIN_HAND);
        }
        if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.CONFIGURATION_CARD.get()){
            return player.getItemInHand(InteractionHand.OFF_HAND);
        }
        return null;
    }
}
