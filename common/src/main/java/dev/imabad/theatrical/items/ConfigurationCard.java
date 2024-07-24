package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConfigurationCard extends Item {
    public ConfigurationCard() {
        super(new Item.Properties().arch$tab(Theatrical.TAB));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(player.isCrouching()){
            //TODO: Open UI
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }
        return super.use(level, player, usedHand);
    }
}
