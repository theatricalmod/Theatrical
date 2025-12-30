package dev.imabad.theatrical.items;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class Items {
    public static final DeferredRegister<Item> ITEMS = TheatricalRegistry.get(Registries.ITEM);
    public static final RegistrySupplier<Item> MOVING_LIGHT = registerBlockItem(
        "moving_light",
            Blocks.MOVING_LIGHT_BLOCK
    );
    public static final RegistrySupplier<Item> PIPE = registerBlockItem(
        "pipe",
            Blocks.PIPE_BLOCK
    );
    public static final RegistrySupplier<Item> LED_FRESNEL = registerBlockItem(
            "led_fresnel",
            Blocks.LED_FRESNEL
    );
    public static final RegistrySupplier<Item> TRUSS = registerBlockItem(
            "truss",
            Blocks.TRUSS_BLOCK
    );
    public static final RegistrySupplier<Item> REDSTONE_INTERFACE = registerBlockItem(
            "redstone_interface",
            Blocks.REDSTONE_INTERFACE
    );
    public static final RegistrySupplier<Item> TANK_TRAP = registerBlockItem(
            "tank_trap",
            Blocks.TANK_TRAP
    );
    public static final RegistrySupplier<Item> LED_PANEL = registerBlockItem(
            "led_panel",
            Blocks.LED_PANEL
    );
    public static final RegistrySupplier<Item> BASIC_LIGHTING_DESK = registerBlockItem(
            "basic_lighting_desk",
            Blocks.BASIC_LIGHTING_DESK
    );
    public static final RegistrySupplier<Item> CONFIGURATION_CARD = register(
            "configuration_card",
            ConfigurationCard::new
    );
    public static final RegistrySupplier<Item> MOVING_WASH = registerBlockItem(
        "moving_wash",
        Blocks.MOVING_WASH_BLOCK
    );

    private static <T extends Item> RegistrySupplier<T> register(String id, Function<Item.Properties, T> supplier) {
        return ITEMS.register(id, () -> supplier.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Theatrical.location(id)))
                .arch$tab(Theatrical.TAB)));
    }

    private static RegistrySupplier<Item> registerBlockItem(String id, RegistrySupplier<? extends Block> supplier) {
        return ITEMS.register(id, () -> new BlockItem(supplier.get(), new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Theatrical.location(id)))
                .useBlockDescriptionPrefix()
                .arch$tab(Theatrical.TAB)));
    }
    
}
