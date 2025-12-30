package dev.imabad.theatrical;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

public class TheatricalExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockStateModel getBakedModel(Identifier modelLocation){
        throw new AssertionError();
    }
    @ExpectPlatform
    public static String getModVersion() {
        throw new AssertionError();
    }


}
