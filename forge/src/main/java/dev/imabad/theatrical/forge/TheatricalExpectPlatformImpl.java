package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class TheatricalExpectPlatformImpl {
    /**
     * This is our actual method to {@link TheatricalExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static BakedModel getBakedModel(ResourceLocation modelLocation){
        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }
}
