package dev.imabad.theatrical;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.utils.value.IntValue;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.config.ConfigHandler;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.registry.FixtureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Theatrical {
    public static final String MOD_ID = "theatrical";
    // Registering a new creative tab
    public static final CreativeModeTab THEATRICAL_TAB = CreativeTabRegistry.create(new ResourceLocation(MOD_ID, "theatrical"), () ->
            new ItemStack(Items.ART_NET_INTERFACE.get()));

    public static final GlobalCableManager CABLES = new GlobalCableManager();

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static void init() {
        ConfigHandler configHandler = new ConfigHandler(Platform.getConfigFolder());
        TheatricalConfig.INSTANCE.register(configHandler);
        registerFixtures();
        TheatricalNet.init();
        Blocks.BLOCKS.register();
        BlockEntities.BLOCK_ENTITIES.register();
        dev.imabad.theatrical.items.Items.ITEMS.register();
        PlayerEvent.PLAYER_JOIN.register(CABLES::playerLogin);
    }

    private static void registerFixtures(){
        FixtureRegistry.buildRegistry();
        Fixtures.FIXTURES.register();
    }

    /**
     * Handle breaking a cable
     * @param level
     * @param pos
     * @param state
     * @param player
     * @return true = cancel the event
     */
    public static boolean handleBlockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player){
        if(state.getBlock() instanceof CableBlock ){
            if(level.getBlockEntity(pos) instanceof CableBlockEntity cbe) {
                int shapeIndex = CableBlock.getSubShapeHit(cbe, player, pos, CableBlock.BOXES);

                if (shapeIndex >= 0 && shapeIndex < CableBlock.BOXES.length) {
                    Direction direction = Direction.values()[shapeIndex];
                    if(cbe.hasSide(direction)){
                        cbe.removeSide(direction);
                        GlobalCableManager.onCableSideRemoved(level, pos, state, direction);
                        return cbe.hasActiveSide();
                    }
                }
            }
        }
        return false;
    }
}
