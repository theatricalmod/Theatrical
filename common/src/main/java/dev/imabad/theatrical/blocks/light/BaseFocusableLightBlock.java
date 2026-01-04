package dev.imabad.theatrical.blocks.light;

import dev.imabad.theatrical.api.FocusableFixture;
import dev.imabad.theatrical.items.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseFocusableLightBlock extends BaseLightBlock {
    protected BaseFocusableLightBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult superResult = super.use(state, level, pos, player, hand, hit);
        if(superResult == InteractionResult.PASS) {
            if (!level.isClientSide()) {
                ItemStack itemInHand = player.getItemInHand(hand);
                if(itemInHand.is(Items.FIXTURE_FOCUSER.get())){
                    CompoundTag itemTag = itemInHand.getOrCreateTag();
                    if(level.getBlockEntity(pos) instanceof FocusableFixture focusableFixture) {
                        if (!itemTag.contains("Light") && focusableFixture.getTrackingEntity() == null) {
                            itemTag.put("Light", NbtUtils.writeBlockPos(pos));
                            focusableFixture.setTrackingEntity(player);
                        } else if(focusableFixture.getTrackingEntity() != null) {
                            focusableFixture.setTrackingEntity(null);
                        }
                        itemInHand.save(itemTag);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return superResult;
    }
}
