package dev.imabad.theatrical;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;

public class TheatricalExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BakedModel getBakedModel(ResourceLocation modelLocation){
        throw new AssertionError();
    }
    @ExpectPlatform
    public static String getModVersion() {
        throw new AssertionError();
    }

}
