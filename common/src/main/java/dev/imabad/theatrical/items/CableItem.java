package dev.imabad.theatrical.items;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CableItem extends Item {
    public CableItem() {
        super(new Item.Properties().tab(Theatrical.THEATRICAL_TAB));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof CableBlock) && !level.getBlockState(pos).getBlock().canBeReplaced(level.getBlockState(pos), new BlockPlaceContext(context)))
        {
            pos = pos.relative(context.getClickedFace());
        }

        ItemStack stack = context.getItemInHand();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Player player = context.getPlayer();

        if (player.mayUseItemAt(pos, context.getClickedFace(), stack))
        {
            if (!(blockEntity instanceof CableBlockEntity))
            {
                level.setBlock(pos, Blocks.CABLE.get().defaultBlockState(), 11);
                blockEntity = level.getBlockEntity(pos);
            }

            if (blockEntity instanceof CableBlockEntity)
            {
                CableBlockEntity cable = (CableBlockEntity) blockEntity;
                Direction opposite = context.getClickedFace().getOpposite();

                if (cable.hasSide(opposite))
                {
                    if(!player.isShiftKeyDown()){
                        return InteractionResult.FAIL;
                    } else {
                        cable.removeSide(opposite);
                        if(!cable.hasActiveSide()){
                            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 11);
                        }
                    }
                }else{
                    boolean haveAdded = false;
                    cable.addSide(opposite);
                }
                blockEntity.setChanged();
            }

            BlockState state = level.getBlockState(pos);
            level.onBlockStateChange(pos, state, state);

            SoundType soundtype = state.getBlock().getSoundType(state);
            level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}
