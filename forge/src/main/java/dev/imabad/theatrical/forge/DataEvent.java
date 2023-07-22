package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.forge.client.model.TheatricalForgeModelLoader;
import dev.imabad.theatrical.items.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataEvent {


    public static void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(true, new BlockState(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(true, new Item(event.getGenerator(), event.getExistingFileHelper()));
        event.getGenerator().addProvider(true, new Lang(event.getGenerator(), "en_us"));
    }


    public static class BlockState extends BlockStateProvider  {
        public BlockState(DataGenerator dataGen, ExistingFileHelper exFileHelper) {
            super(dataGen, Theatrical.MOD_ID, exFileHelper);
        }

        @Override
        protected void registerStatesAndModels() {
            simpleBlock(Blocks.ART_NET_INTERFACE.get());
            registerCable();
//            horizontalBlock(Blocks.PIPE_BLOCK.get(), new ModelFile.UncheckedModelFile(new ResourceLocation("theatrical:block/pipe")));
        }

        private void registerCable() {
            // Using CustomLoaderBuilder we can define a JSON file for our model that will use our baked model
            BlockModelBuilder generatorModel = models().getBuilder(Blocks.CABLE.getId().getPath())
                    .parent(models().getExistingFile(mcLoc("cube")))
                    .customLoader((blockModelBuilder, helper) -> new CustomLoaderBuilder<BlockModelBuilder>(TheatricalForgeModelLoader.CABLE_MODEL_LOADER,
                            blockModelBuilder, helper) { })
                    .end();
            simpleBlock(Blocks.CABLE.get(), generatorModel);
        }
    }

    public static class Item extends ItemModelProvider {

        public Item(DataGenerator dataGen, ExistingFileHelper existingFileHelper) {
            super(dataGen, Theatrical.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            cubeAll(Blocks.ART_NET_INTERFACE.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/artnet_interface"));
            withExistingParent(Blocks.PIPE_BLOCK.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/pipe"));
            withExistingParent(Blocks.MOVING_LIGHT_BLOCK.getId().getPath(), new ResourceLocation(Theatrical.MOD_ID, "block/moving_light/moving_head_whole"));
            basicItem(Items.DMX_CABLE.getId());
            basicItem(Items.BUNDLED_CABLE.getId());
        }
    }

    public static class Lang extends LanguageProvider {

        public Lang(DataGenerator gen, String locale) {
            super(gen, Theatrical.MOD_ID, locale);
        }

        @Override
        protected void addTranslations() {
            addBlock(Blocks.ART_NET_INTERFACE, "ArtNet Interface");
            addBlock(Blocks.MOVING_LIGHT_BLOCK, "Moving Light");
            addBlock(Blocks.PIPE_BLOCK, "Rigging Pipe");
            addItem(Items.DMX_CABLE, "DMX Cable");
            addItem(Items.BUNDLED_CABLE, "Bundled Cable");
            add("itemGroup.theatrical.theatrical", "Theatrical");
            add("artneti.dmxUniverse", "DMX Universe");
            add("artneti.ipAddress", "IP Address");
            add("artneti.save", "Save");
            add("artneti.notConnected", "No data received");
            add("artneti.notAuthorized", "You're not authorized!");
            add("artneti.lastReceived", "Data received %d second(s) ago");
            add("fixture.dmxStart", "DMX Address");
            add("screen.movinglight", "Moving Light");
        }
    }

}
