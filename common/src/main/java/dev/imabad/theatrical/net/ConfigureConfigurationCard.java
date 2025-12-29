package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.items.ConfigurationCardData;
import dev.imabad.theatrical.items.DataComponents;
import dev.imabad.theatrical.items.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ConfigureConfigurationCard(UUID network, int dmxAddress,
                                         int dmxUniverse, boolean autoIncrement,
                                         boolean universeEnabled, boolean addressEnabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigureConfigurationCard> TYPE
            = new CustomPacketPayload.Type<>(Theatrical.location("configure_configuration_card"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureConfigurationCard> STREAM_CODEC = StreamCodec.ofMember(ConfigureConfigurationCard::encode, ConfigureConfigurationCard::new);

    ConfigureConfigurationCard(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    private void encode(FriendlyByteBuf out) {
        out.writeUUID(network);
        out.writeInt(dmxAddress);
        out.writeInt(dmxUniverse);
        out.writeBoolean(autoIncrement);
        out.writeBoolean(universeEnabled);
        out.writeBoolean(addressEnabled);
    }

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
                ConfigurationCardData data = new ConfigurationCardData(
                        network,
                        dmxUniverse,
                        dmxAddress,
                        autoIncrement,
                        universeEnabled,
                        addressEnabled
                );
                itemStack.set(DataComponents.CONFIGURATION_CARD_DATA.get(), data);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
