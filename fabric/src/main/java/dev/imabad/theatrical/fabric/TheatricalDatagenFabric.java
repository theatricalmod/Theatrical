package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
import dev.imabad.theatrical.items.Items;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class TheatricalDatagenFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(Lang::new);
        pack.addProvider(Models::new);
    }

    public static class Models extends FabricModelProvider {

        public Models(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
            blockModelGenerators.createTrivialCube(Blocks.ART_NET_INTERFACE.get());
            blockModelGenerators.createTrivialCube(Blocks.REDSTONE_INTERFACE.get());
            blockModelGenerators.createAxisAlignedPillarBlockCustomModel(Blocks.TRUSS_BLOCK.get(), ResourceLocation.tryParse("theatrical:block/truss"));
            createLightingDesk(blockModelGenerators, Blocks.BASIC_LIGHTING_DESK.get());
            ResourceLocation tankTrapWithPipe = new ResourceLocation("theatrical:block/tank_trap_with_pipe");
            ResourceLocation tankTrap = new ResourceLocation("theatrical:block/tank_trap");
            blockModelGenerators.blockStateOutput.accept(
                    MultiVariantGenerator.multiVariant(Blocks.TANK_TRAP.get())
                            .with(PropertyDispatch.property(TankTrapBlock.HAS_PIPE)
                                    .select(true, Variant.variant().with(VariantProperties.MODEL, tankTrapWithPipe))
                                    .select(false, Variant.variant().with(VariantProperties.MODEL, tankTrap)))
            );
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.ART_NET_INTERFACE.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.REDSTONE_INTERFACE.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.PIPE_BLOCK.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.TRUSS_BLOCK.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.MOVING_LIGHT_BLOCK.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.MOVING_WASH_BLOCK.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.LED_FRESNEL.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.TANK_TRAP.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.LED_PANEL.get());
//            blockModelGenerators.createSimpleFlatItemModel(Blocks.BASIC_LIGHTING_DESK.get());
        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerators) {
            itemModelGenerators.generateFlatItem(Items.CONFIGURATION_CARD.get(), ModelTemplates.FLAT_ITEM);
            parent(itemModelGenerators, Blocks.LED_FRESNEL.get(),  new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_whole"));
            parent(itemModelGenerators, Blocks.PIPE_BLOCK.get(),  new ResourceLocation(Theatrical.MOD_ID, "block/vertical_pipe"));
            parent(itemModelGenerators, Blocks.MOVING_LIGHT_BLOCK.get(),  new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_whole"));
            parent(itemModelGenerators, Blocks.MOVING_WASH_BLOCK.get(),  new ResourceLocation(Theatrical.MOD_ID, "block/moving_wash/moving_wash_whole"));
            parent(itemModelGenerators, Blocks.LED_PANEL.get(),  new ResourceLocation(Theatrical.MOD_ID, "block/led_panel"));
            itemModelGenerators.generateFlatItem(Items.FIXTURE_FOCUSER.get(), ModelTemplates.FLAT_ITEM);
        }

        private static void parent(ItemModelGenerators itemModelGenerators, Block block) {
            ResourceLocation itemLoc = ModelLocationUtils.getModelLocation(block.asItem());
            ResourceLocation blockLoc = ModelLocationUtils.getModelLocation(block);
            itemModelGenerators.output.accept(
                    itemLoc,
                    new DelegatedModel(blockLoc)
            );
        }

        private static void parent(ItemModelGenerators itemModelGenerators, Block block, ResourceLocation parent) {
            ResourceLocation itemLoc = ModelLocationUtils.getModelLocation(block.asItem());
            itemModelGenerators.output.accept(
                    itemLoc,
                    new DelegatedModel(parent)
            );
        }

        public final void createLightingDesk(BlockModelGenerators blockModelGenerators, BasicLightingDeskBlock horizontallyRotatedBlock) {
            ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(horizontallyRotatedBlock);
            blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator
                    .multiVariant(horizontallyRotatedBlock, Variant.variant()
                            .with(VariantProperties.MODEL, resourceLocation))
                    .with(BlockModelGenerators.createHorizontalFacingDispatch())
                    .with(PropertyDispatch.property(BasicLightingDeskBlock.TRIGGERED).select(true, Variant.variant())
                            .select(false, Variant.variant())));
        }
    }

    public static class Lang extends FabricLanguageProvider {
        protected Lang(FabricDataOutput dataOutput) {
            // Specifying en_us is optional, as it's the default language code
            super(dataOutput, "en_us");
        }
        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
            translationBuilder.add(Blocks.ART_NET_INTERFACE.get(), "ArtNet Interface");
            translationBuilder.add(Blocks.MOVING_LIGHT_BLOCK.get(), "Moving Light");
            translationBuilder.add(Blocks.MOVING_WASH_BLOCK.get(), "Moving Wash");
            translationBuilder.add(Blocks.PIPE_BLOCK.get(), "Rigging Pipe");
            translationBuilder.add(Blocks.LED_FRESNEL.get(), "LED Fresnel");
            translationBuilder.add(Blocks.TRUSS_BLOCK.get(), "MT100 Truss");
            translationBuilder.add(Blocks.REDSTONE_INTERFACE.get(), "Redstone Interface");
            translationBuilder.add(Blocks.TANK_TRAP.get(), "Tank Trap");
            translationBuilder.add(Blocks.LED_PANEL.get(), "LED Panel");
            translationBuilder.add(Blocks.BASIC_LIGHTING_DESK.get(), "Basic Lighting Desk");
            translationBuilder.add(Items.CONFIGURATION_CARD.get(), "Configuration Card");
            translationBuilder.add(Items.FIXTURE_FOCUSER.get(), "Fixture Focuser");
            translationBuilder.add("itemGroup.theatrical", "Theatrical");
            translationBuilder.add("artneti.dmxUniverse", "Network Universe");
            translationBuilder.add("artneti.ipAddress", "IP Address");
            translationBuilder.add("artneti.save", "Save");
            translationBuilder.add("artneti.notConnected", "No data received");
            translationBuilder.add("artneti.notAuthorized", "You're not authorized!");
            translationBuilder.add("artneti.lastReceived", "Data received %d second(s) ago");
            translationBuilder.add("fixture.dmxStart", "Start address");
            translationBuilder.add("fixture.pan", "Pan");
            translationBuilder.add("fixture.tilt", "Tilt");
            translationBuilder.add("screen.movinglight", "Moving Light");
            translationBuilder.add("button.artnetconfig", "ArtNet Config");
            translationBuilder.add("screen.artnetconfig.enabled", "ArtNet Enabled: %s");
            translationBuilder.add("ui.control.step", "Step - %s");
            translationBuilder.add("ui.control.modes.run", "Run Mode");
            translationBuilder.add("ui.control.modes.program", "Program Mode");
            translationBuilder.add("ui.control.cues", "Cues");
            translationBuilder.add("ui.control.cue", "Cue - %s");
            translationBuilder.add("ui.control.fadeIn", "Fade in");
            translationBuilder.add("ui.control.fadeOut", "Fade out");
            translationBuilder.add("commands.network.notfound", "Network not found.");
            translationBuilder.add("commands.networks", "There are %s network(s): %s.");
            translationBuilder.add("commands.network.members", "There are %s network member(s): %s.");
            translationBuilder.add("commands.network.members.add.success", "Added %s to the network.");
            translationBuilder.add("commands.network.members.add.failed", "Player already member of network.");
            translationBuilder.add("commands.network.members.remove.success", "Removed %s from the network.");
            translationBuilder.add("commands.network", "%s (%s) has %s member(s)");
            translationBuilder.add("commands.network.invalid", "Unknown network mode: %s");
            translationBuilder.add("commands.network.role.invalid", "Unknown member role: %s");
            translationBuilder.add("commands.network.created", "Network created");
            translationBuilder.add("commands.network.deleted", "Network deleted");
            translationBuilder.add("commands.network.updated", "Network updated");
            translationBuilder.add("screen.configurationcard.autoincrement", "Address Auto Increment");
            translationBuilder.add("screen.configurationcard", "Configuration Card");
            translationBuilder.add("screen.artnetconfig.network", "Network");
            translationBuilder.add("screen.artnetconfig.entry", "Subnet: %s Universe: %s");
            translationBuilder.add("item.configurationcard.success", "Configured device to %s network, universe %s and address %s - next address is %s.");
            translationBuilder.add("screen.artnetconfig.entry.subnet", "Subnet: %s");
            translationBuilder.add("screen.artnetconfig.entry.universe", "Universe: %s");
            translationBuilder.add("screen.artnetconfig.subnet", "Art-Net Subnet");
            translationBuilder.add("screen.artnetconfig.universe", "Art-Net Universe");
            translationBuilder.add("screen.artnetconfig.networkUniverse", "Network Universe");
            translationBuilder.add("screen.artnetconfig.networkEnabled", "Enabled");
            translationBuilder.add("item.configurationcard.description.1", "Shift + Right Click for settings");
            translationBuilder.add("item.configurationcard.description.2", "Right click on fixture to apply");
        }
    }
}
