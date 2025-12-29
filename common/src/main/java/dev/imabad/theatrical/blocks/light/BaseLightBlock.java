package dev.imabad.theatrical.blocks.light;

import com.mojang.serialization.MapCodec;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.LightCollisionContext;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BaseLightBlock extends HangableBlock implements EntityBlock {

    private final MapCodec<? extends HorizontalDirectionalBlock> CODEC;

    protected BaseLightBlock(Properties properties, MapCodec<? extends HorizontalDirectionalBlock> codec) {
        super(properties);
        CODEC = codec;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof LightCollisionContext lcC){
           if(lcC.getFromPos().equals(pos)) {
               return Shapes.empty();
           }
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if(!level.isClientSide){
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof BaseDMXConsumerLightBlockEntity consumerLightBlockEntity && placer instanceof ServerPlayer player){
                consumerLightBlockEntity.setNetworkId(TheatricalNetworkData.getInstance(level.getServer().overworld()).getDefaultNetworkForPlayer(player).id());
            }
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if(!level.isClientSide()) {
            if (be instanceof BaseDMXConsumerLightBlockEntity consumerLightBlockEntity) {
                if (!consumerLightBlockEntity.getNetworkId().equals(UUIDUtil.NULL)) {
                    TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(consumerLightBlockEntity.getNetworkId());
                    if (network != null && !network.members().isMember(player.getUUID())) {
                        return InteractionResult.FAIL;
                    }
                }
                if (player.getItemInHand(hand).getItem() == Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    CompoundTag tagData = itemInHand.getOrCreateTag();
                    consumerLightBlockEntity.setNetworkId(tagData.getUUID("network"));
                    if (tagData.getBoolean("universeEnabled")) {
                        consumerLightBlockEntity.setUniverse(tagData.getInt("dmxUniverse"));
                    }
                    if (tagData.getBoolean("addressEnabled")) {
                        consumerLightBlockEntity.setChannelStartPoint(tagData.getInt("dmxAddress"));
                    }
                    if (tagData.getBoolean("autoIncrement")) {
                        tagData.putInt("dmxAddress", tagData.getInt("dmxAddress") + consumerLightBlockEntity.getChannelCount());
                    }
                    itemInHand.save(tagData);
                    TheatricalNetworkData instance = TheatricalNetworkData.getInstance(level.getServer().overworld());
                    player.sendSystemMessage(Component.translatable("item.configurationcard.success", instance.getNetwork(consumerLightBlockEntity.getNetworkId()).name(), Integer.toString(consumerLightBlockEntity.getUniverse()), Integer.toString(consumerLightBlockEntity.getChannelStart()), Integer.toString(tagData.getInt("dmxAddress"))));
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
