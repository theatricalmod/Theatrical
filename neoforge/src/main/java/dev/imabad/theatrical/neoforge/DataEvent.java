package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
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
            ModelFile.ExistingModelFile trussModel = models().getExistingFile(new ResourceLocation("theatrical:block/truss"));
            axisBlock(Blocks.TRUSS_BLOCK.get(), trussModel, trussModel);
            getVariantBuilder(Blocks.TANK_TRAP.get()).forAllStates(blockState -> {
                ModelFile file = models().getExistingFile(new ResourceLocation("theatrical:block/tank_trap"));
                if(blockState.getValue(TankTrapBlock.HAS_PIPE)){
                    file = models().getExistingFile(new ResourceLocation("theatrical:block/tank_trap_with_pipe"));
                }
                return ConfiguredModel.builder().modelFile(file).build();
            });
//            horizontalBlock(Blocks.PIPE_BLOCK.get(), new ModelFile.UncheckedModelFile(new ResourceLocation("theatrical:block/pipe")));
        }

    }

    public static class Item extends ItemModelProvider {

        public Item(PackOutput output, ExistingFileHelper existingFileHelper) {
            super(output, Theatrical.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            cubeAll(Blocks.ART_NET_INTERFACE.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/artnet_interface"));
            cubeAll(Blocks.REDSTONE_INTERFACE.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/redstone_interface"));
            withExistingParent(Blocks.PIPE_BLOCK.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/vertical_pipe"));
            withExistingParent(Blocks.TRUSS_BLOCK.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/truss"));
            withExistingParent(Blocks.MOVING_LIGHT_BLOCK.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_whole"));
            withExistingParent(Blocks.LED_FRESNEL.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/fresnel/fresnel_whole"));
            withExistingParent(Blocks.TANK_TRAP.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/tank_trap"));
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
            addBlock(Blocks.PIPE_BLOCK, "Rigging Pipe");
            addBlock(Blocks.LED_FRESNEL, "LED Fresnel");
            addBlock(Blocks.TRUSS_BLOCK, "MT100 Truss");
            addBlock(Blocks.REDSTONE_INTERFACE, "Redstone Interface");
            addBlock(Blocks.TANK_TRAP, "Tank Trap");
            add("itemGroup.theatrical", "Theatrical");
            add("artneti.dmxUniverse", "DMX Universe");
            add("artneti.ipAddress", "IP Address");
            add("artneti.save", "Save");
            add("artneti.notConnected", "No data received");
            add("artneti.notAuthorized", "You're not authorized!");
            add("artneti.lastReceived", "Data received %d second(s) ago");
            add("fixture.dmxStart", "DMX Address");
            add("fixture.pan", "Pan");
            add("fixture.tilt", "Tilt");
            add("screen.movinglight", "Moving Light");
        }
    }

}
