package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.client.gui.screen.ConfigurationCardScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfigurationCard extends Item {
    public ConfigurationCard() {
        super(new Item.Properties().arch$tab(Theatrical.TAB));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(player.isCrouching() && level.isClientSide()){
            ItemStack itemInHand = player.getItemInHand(usedHand);
            ConfigurationCardData cardData;
            if(itemInHand.has(DataComponents.CONFIGURATION_CARD_DATA.get())){
                cardData = itemInHand.get(DataComponents.CONFIGURATION_CARD_DATA.get());
            } else {
                cardData = new ConfigurationCardData(null, 0, 0, false, false, false);
            }
            openUI(cardData);
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }
        return super.use(level, player, usedHand);
    }

    @Environment(EnvType.CLIENT)
    private static void openUI(ConfigurationCardData data){
        Minecraft.getInstance().setScreen(new ConfigurationCardScreen(data));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.configurationcard.description.1"));
        tooltipComponents.add(Component.translatable("item.configurationcard.description.2"));
    }

}
