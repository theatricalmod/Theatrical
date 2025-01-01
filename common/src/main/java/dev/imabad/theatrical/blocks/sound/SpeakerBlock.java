package dev.imabad.theatrical.blocks.sound;

import dev.imabad.theatrical.blockentities.sound.MusicPlayerBlockEntity;
import dev.imabad.theatrical.blockentities.sound.SpeakerBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.networks.AVNetworkData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SpeakerBlock extends Block implements EntityBlock {
    public SpeakerBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpeakerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide && hand == InteractionHand.MAIN_HAND){
            SpeakerBlockEntity be = (SpeakerBlockEntity)level.getBlockEntity(pos);
            if(be != null) {
                if (player.getItemInHand(hand).getItem() == Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    CompoundTag tagData = itemInHand.getOrCreateTag();
                    be.setNetworkId(tagData.getUUID("network"));
                    AVNetworkData instance = AVNetworkData.getInstance(level.getServer().overworld());
                    player.sendSystemMessage(Component.translatable("item.configurationcard.success", instance.getNetwork(be.getNetworkId()).name()));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
