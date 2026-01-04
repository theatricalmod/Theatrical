package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.FocusableFixture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FixtureFocuser extends Item {
    public FixtureFocuser() {
        super(new Item.Properties().arch$tab(Theatrical.TAB));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!level.isClientSide){
            if(player.isShiftKeyDown()){
                ItemStack itemInHand = player.getItemInHand(usedHand);
                CompoundTag cardData = itemInHand.getOrCreateTag();
                if(cardData.contains("Light")){
                    BlockPos lightPos = NbtUtils.readBlockPos(cardData.getCompound("Light"));
                    BlockEntity blockEntity = level.getBlockEntity(lightPos);
                    if(blockEntity instanceof FocusableFixture focusableFixture){
                        focusableFixture.setTrackingEntity(null);
                        cardData.remove("Light");
                        itemInHand.save(cardData);
                    }
                }
            }
        }
        return super.use(level, player, usedHand);
    }
}
