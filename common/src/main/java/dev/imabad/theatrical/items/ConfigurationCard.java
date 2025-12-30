package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.client.gui.screen.ConfigurationCardScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ConfigurationCard extends Item {
    public ConfigurationCard(Item.Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        if(player.isCrouching() && level.isClientSide()){
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            ConfigurationCardData cardData;
            if(itemInHand.has(DataComponents.CONFIGURATION_CARD_DATA.get())){
                cardData = itemInHand.get(DataComponents.CONFIGURATION_CARD_DATA.get());
            } else {
                cardData = new ConfigurationCardData(null, 0, 0, false, false, false);
            }
            openUI(cardData);
            return InteractionResult.PASS;
        }
        return super.use(level, player, interactionHand);
    }

    private static void openUI(ConfigurationCardData data){
        Minecraft.getInstance().setScreen(new ConfigurationCardScreen(data));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, consumer, tooltipFlag);
        consumer.accept(Component.translatable("item.configurationcard.description.1"));
        consumer.accept(Component.translatable("item.configurationcard.description.2"));
    }
}
