package dev.imabad.theatrical.blocks.rigging;

import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.items.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TankTrapBlock extends Block {

    public static final BooleanProperty HAS_PIPE = BooleanProperty.create("has_pipe");
    private final VoxelShape SIMPLE_BOX = Shapes.create(new AABB(0, 0, 0, 1, 0.2, 1));
    private final VoxelShape PIPE = Shapes.create(new AABB(0.4, 0, 0.4, 0.6, 1, 0.6));
    public TankTrapBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
        this.registerDefaultState(this.getStateDefinition().any().setValue(HAS_PIPE, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HAS_PIPE) ? Shapes.or(SIMPLE_BOX, PIPE) : SIMPLE_BOX;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_PIPE);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide && player.getItemInHand(hand).is(Items.PIPE.get())){
            if(!state.getValue(HAS_PIPE)){
                level.setBlock(pos, state.setValue(HAS_PIPE, true), Block.UPDATE_CLIENTS);
                if (!player.isCreative()) {
                    if (player.getItemInHand(hand).getCount() > 1) {
                        player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                    } else {
                        player.setItemInHand(hand, new ItemStack(net.minecraft.world.item.Items.AIR));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
