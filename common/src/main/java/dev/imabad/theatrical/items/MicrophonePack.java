package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class MicrophonePack extends ArmorItem {
    public MicrophonePack() {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Properties().arch$tab(Theatrical.TAB).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(!level.isClientSide) {
            ItemStack itemInHand = player.getItemInHand(usedHand);
            if (itemInHand.is(this)) {
                CompoundTag currentTag = itemInHand.getOrCreateTag();
                if(player.isCrouching()) {
                    boolean toggled = !isTurnedOn(itemInHand);
                    currentTag.putBoolean("on", toggled);
                }
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        boolean turnedOn = isTurnedOn(stack);
        Component statusComp = Component.translatable(turnedOn ? "micpack.on" : "micpack.off")
                .withStyle(Style.EMPTY.withColor(turnedOn ? ChatFormatting.GREEN : ChatFormatting.RED));
        tooltipComponents.add(Component.translatable("micpack.status").append(statusComp));
    }

    private boolean isTurnedOn(ItemStack itemStack){
        CompoundTag tag = itemStack.getOrCreateTag();
        return tag.contains("on") && tag.getBoolean("on");
    }

    private UUID getFrequency(ItemStack itemStack){
        CompoundTag tag = itemStack.getOrCreateTag();
        if(tag.contains("freq")){
            return tag.getUUID("freq");
        } else {
            return UUID.randomUUID();
        }
    }

    private void setFrequency(ItemStack itemStack, UUID freq){
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putUUID("freq", freq);
    }
}
