package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Optional;

public class TheatricalExpectPlatformImpl {
    /**
     * This is our actual method to {@link TheatricalExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static BakedModel getBakedModel(ResourceLocation modelLocation){
        return Minecraft.getInstance().getModelManager().getModel(modelLocation);
    }

    public static String getModVersion() {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(Theatrical.MOD_ID);
        if(modContainer.isPresent()){
            return modContainer.get().getMetadata().getVersion().getFriendlyString();
        }
        return "Unknown";
    }
}
