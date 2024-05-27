package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

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
    public static String getModVersion() {
        ModFileInfo modFileById = LoadingModList.get().getModFileById(Theatrical.MOD_ID);
        if(modFileById != null) {
            return modFileById.versionString();
        }
        return "Unknown";
    }
}
