package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
import dev.imabad.theatrical.items.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DataEvent {
    public static void onData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();

        gen.addProvider(event.includeClient(), new BlockState(output, event.getExistingFileHelper()));
        gen.addProvider(event.includeClient(), new Item(output, event.getExistingFileHelper()));
        gen.addProvider(event.includeClient(), new Lang(output, "en_us"));
    }

    public static class BlockState extends BlockStateProvider {
        public BlockState(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, Theatrical.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            simpleBlock(Blocks.ART_NET_INTERFACE.get());
            simpleBlock(Blocks.REDSTONE_INTERFACE.get());
            ModelFile.ExistingModelFile trussModel = models().getExistingFile(ResourceLocation.tryParse("theatrical:block/truss"));
            axisBlock(Blocks.TRUSS_BLOCK.get(), trussModel, trussModel);
            horizontalBlock(Blocks.BASIC_LIGHTING_DESK.get(), models().getExistingFile(ResourceLocation.tryParse("theatrical:block/lighting_console")));
            getVariantBuilder(Blocks.TANK_TRAP.get()).forAllStates(blockState -> {
                ModelFile file = models().getExistingFile(ResourceLocation.tryParse("theatrical:block/tank_trap"));
                if(blockState.getValue(TankTrapBlock.HAS_PIPE)){
                    file = models().getExistingFile(ResourceLocation.tryParse("theatrical:block/tank_trap_with_pipe"));
                }
                return ConfiguredModel.builder().modelFile(file).build();
            });
//            horizontalBlock(Blocks.PIPE_BLOCK.get(), new ModelFile.UncheckedModelFile(ResourceLocation.tryParse("theatrical:block/pipe")));
        }

    }

    public static class Item extends ItemModelProvider {

        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Theatrical.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            cubeAll(Blocks.ART_NET_INTERFACE.getId().getPath(), Theatrical.location( "block/artnet_interface"));
            cubeAll(Blocks.REDSTONE_INTERFACE.getId().getPath(), Theatrical.location( "block/redstone_interface"));
            withExistingParent(Blocks.PIPE_BLOCK.getId().getPath(), Theatrical.location( "block/vertical_pipe"));
            withExistingParent(Blocks.TRUSS_BLOCK.getId().getPath(), Theatrical.location( "block/truss"));
            withExistingParent(Blocks.MOVING_LIGHT_BLOCK.getId().getPath(), Theatrical.location( "block/moving_light/moving_head_whole"));
            withExistingParent(Blocks.MOVING_WASH_BLOCK.getId().getPath(), Theatrical.location( "block/moving_wash/moving_wash_whole"));
            withExistingParent(Blocks.LED_FRESNEL.getId().getPath(), Theatrical.location( "block/fresnel/fresnel_whole"));
            withExistingParent(Blocks.TANK_TRAP.getId().getPath(), Theatrical.location( "block/tank_trap"));
            withExistingParent(Blocks.LED_PANEL.getId().getPath(), Theatrical.location( "block/led_panel"));
            withExistingParent(Blocks.BASIC_LIGHTING_DESK.getId().getPath(), Theatrical.location( "block/lighting_console"));
            withExistingParent(Items.CONFIGURATION_CARD.getId().getPath(), mcLoc("item/generated"))
                    .texture("layer0", Theatrical.location( "item/configuration_card"));
        }
    }

    public static class Lang extends LanguageProvider {

        public Lang(PackOutput output, String locale) {
            super(output, Theatrical.MOD_ID, locale);
        }

        @Override
        protected void addTranslations() {
            addBlock(Blocks.ART_NET_INTERFACE, "ArtNet Interface");
            addBlock(Blocks.MOVING_LIGHT_BLOCK, "Moving Light");
            addBlock(Blocks.MOVING_WASH_BLOCK, "Moving Wash");
            addBlock(Blocks.PIPE_BLOCK, "Rigging Pipe");
            addBlock(Blocks.LED_FRESNEL, "LED Fresnel");
            addBlock(Blocks.TRUSS_BLOCK, "MT100 Truss");
            addBlock(Blocks.REDSTONE_INTERFACE, "Redstone Interface");
            addBlock(Blocks.TANK_TRAP, "Tank Trap");
            addBlock(Blocks.LED_PANEL, "LED Panel");
            addBlock(Blocks.BASIC_LIGHTING_DESK, "Basic Lighting Desk");
            addItem(Items.CONFIGURATION_CARD, "Configuration Card");
            add("itemGroup.theatrical", "Theatrical");
            add("artneti.dmxUniverse", "Network Universe");
            add("artneti.ipAddress", "IP Address");
            add("artneti.save", "Save");
            add("artneti.notConnected", "No data received");
            add("artneti.notAuthorized", "You're not authorized!");
            add("artneti.lastReceived", "Data received %d second(s) ago");
            add("fixture.dmxStart", "Start address");
            add("fixture.pan", "Pan");
            add("fixture.tilt", "Tilt");
            add("screen.movinglight", "Moving Light");
            add("button.artnetconfig", "ArtNet Config");
            add("screen.artnetconfig.enabled", "ArtNet Enabled: %s");
            add("ui.control.step", "Step - %s");
            add("ui.control.modes.run", "Run Mode");
            add("ui.control.modes.program", "Program Mode");
            add("ui.control.cues", "Cues");
            add("ui.control.cue", "Cue - %s");
            add("ui.control.fadeIn", "Fade in");
            add("ui.control.fadeOut", "Fade out");
            add("commands.network.notfound", "Network not found.");
            add("commands.networks", "There are %s network(s): %s.");
            add("commands.network.members", "There are %s network member(s): %s.");
            add("commands.network.members.add.success", "Added %s to the network.");
            add("commands.network.members.add.failed", "Player already member of network.");
            add("commands.network.members.remove.success", "Removed %s from the network.");
            add("commands.network", "%s (%s) has %s member(s)");
            add("commands.network.invalid", "Unknown network mode: %s");
            add("commands.network.role.invalid", "Unknown member role: %s");
            add("commands.network.created", "Network created");
            add("commands.network.deleted", "Network deleted");
            add("commands.network.updated", "Network updated");
            add("screen.configurationcard.autoincrement", "Address Auto Increment");
            add("screen.configurationcard", "Configuration Card");
            add("screen.artnetconfig.network", "Network");
            add("screen.artnetconfig.entry", "Subnet: %s Universe: %s");
            add("item.configurationcard.success", "Configured device to %s network, universe %s and address %s - next address is %s.");
            add("screen.artnetconfig.entry.subnet", "Subnet: %s");
            add("screen.artnetconfig.entry.universe", "Universe: %s");
            add("screen.artnetconfig.subnet", "Art-Net Subnet");
            add("screen.artnetconfig.universe", "Art-Net Universe");
            add("screen.artnetconfig.networkUniverse", "Network Universe");
            add("screen.artnetconfig.networkEnabled", "Enabled");
            add("item.configurationcard.description.1", "Shift + Right Click for settings");
            add("item.configurationcard.description.2", "Right click on fixture to apply");
        }
    }

}
