package dev.theatricalmod.theatrical.data;

import dev.theatricalmod.theatrical.TheatricalMod;
import dev.theatricalmod.theatrical.api.fixtures.Fixture;
import dev.theatricalmod.theatrical.block.TheatricalBlocks;
import dev.theatricalmod.theatrical.fixtures.MovingLightFixture;
import dev.theatricalmod.theatrical.items.TheatricalItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import xyz.brassgoggledcoders.patchouliprovider.BookBuilder;
import xyz.brassgoggledcoders.patchouliprovider.CategoryBuilder;
import xyz.brassgoggledcoders.patchouliprovider.PatchouliBookProvider;

import java.util.function.Consumer;

public class TheatricalBookProvider extends PatchouliBookProvider {

    public static final String NAME_KEY = "guide.theatrical.name";
    public static final String DESC_KEY = "guide.theatrical.landing_text";

    public TheatricalBookProvider(DataGenerator gen) {
        super(gen, TheatricalMod.MOD_ID, "en_us");
    }

    @Override
    protected void addBooks(Consumer<BookBuilder> consumer) {

        final BookBuilder builder = createBookBuilder("theatrical", NAME_KEY, DESC_KEY);
        builder.setCreativeTab(TheatricalMod.theatricalItemGroup.getPath());
        builder.setShowProgress(false);
        builder.setVersion("1");
        builder.setModel(TheatricalMod.MOD_ID + ":guide");
        final CategoryBuilder lightingCategory =
                builder.addCategory("lighting_category", "Lighting", "Let there be light!", new ItemStack(TheatricalBlocks.GENERIC_LIGHT.get()));
        lightingCategory.addEntry("glossary", "Glossary", new ItemStack(TheatricalItems.BULB.get()))
                .addSimpleTextPage("$(li) Lamp: Proper name for what is commonly known as a bulb" +
                                    "$(li) Fixture: Another word to refer to an individual light")
                .setPriority(true);
        lightingCategory.addEntry("rigging", "Rigging", new ItemStack(TheatricalItems.TRUSS.get()))
                .addSimpleTextPage("Lights may only be rigged off (placed on) appropriate surfaces. Moving lights may be placed on top of any solid block, or hung from $(item)Truss$(). Generics may only hang, and may only do so from $(item)Truss$() or from internally wired bars. The latter contains power wiring within the bar.")
                .addCraftingPage(modLoc("truss")).setRecipe2(modLoc("iwb"));
        lightingCategory.addEntry("dimmer_rack", "Dimmers and Generics", new ItemStack(TheatricalItems.DIMMER_RACK.get()))
                .addSimpleTextPage("Dimmed power is used to control the brightness of generic lighting. 'Hot' Power by contrast, is standard Forge Energy used to power everything else. Make sure you use the correct power type!", "Dimmed vs 'Hot' Power")
                .addCraftingPage(modLoc("dimmer_rack")).setText("To convert into dimmed power, you need a $(item)Dimmer Rack$(). The dimmer rack also routes the power based on the settings in its GUI, and is the DMX interface point for all connected generics.")
                .build().addSimpleTextPage("Now that you have the dimmed power, you can plug in generic lights. Ensure you plug them into appropriate sides of your rack, and remember that each side of the Dimmer Rack represents a different DMX channel. If you connect multiple lights to one side, they will all respond to that DMX channel." +
                "Generic lights must be re-focused by hand.")
                .addCraftingPage(modLoc("generic_light")).setRecipe2(modLoc("bulb"));
        lightingCategory.addEntry("intels", "Intelligent Lighting", new ItemStack(TheatricalItems.MOVING_LIGHT.get()))
                .addSimpleTextPage("Intelligent Lighting presents a lot more options than generic lighting. DMX and hot power should connected directly to this fixture, and it has 7 channels of control.")
                .addSimpleTextPage(Fixture.getRegistry().getValue(MovingLightFixture.ID).getChannelsDefinition().toString().replace("#", "$(li)"))
                .addCraftingPage(modLoc("moving_light")).setRecipe2(modLoc("led"))
                .build().addCraftingPage(modLoc("motor")).setRecipe2(modLoc("cog"));
        lightingCategory.addEntry("control", "Command and Control", new ItemStack(TheatricalItems.BASIC_LIGHTING_DESK.get()))
                .addSimpleTextPage("Lighting equipment is controlled through DMX cable. For intelligent lights, this needs to be plugged directly into the light, whereas for generics the DMX is run to the dimmer rack.")
                .addSimpleTextPage("Available in-game is a very basic lighting desk for sending DMX signals. You can also use the ArtNet interface to interface with real lighting control software, or even a physical desk!")
                .addCraftingPage(modLoc("artnet_interface")).build()
                .addCraftingPage(modLoc("redstone_interface"))
                .setText("There also exists a DMX -> Redstone converter, allowing control of redstone lighting, pistons, etc, with DMX signal.");
        final CategoryBuilder resourcesCategory =
                builder.addCategory("resources_category", "Resources", "Useful resources for learning more about the mod, and theatre/live events in general", new ItemStack(Items.BOOKSHELF));
        resourcesCategory.addEntry("videos", "Videos", new ItemStack(Items.ENDER_EYE))
                .addLinkPage("https://www.youtube.com/watch?v=_aR-IkcK9dA", "gewenzsko's tutorial");
        resourcesCategory.addEntry("other", "Other", new ItemStack(Items.BEDROCK))
                .addLinkPage("https://discord.gg/7qMs5d6", "Mod Discord");
        builder.build(consumer);
    }

    public static ResourceLocation modLoc(String location) {
        return new ResourceLocation(TheatricalMod.MOD_ID, location);
    }
}
