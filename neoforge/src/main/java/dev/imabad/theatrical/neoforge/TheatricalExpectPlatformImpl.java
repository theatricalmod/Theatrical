package dev.imabad.theatrical.neoforge;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

import java.nio.file.Path;

public class TheatricalExpectPlatformImpl {
    /**
     * This is our actual method to {@link TheatricalExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static BlockStateModel getBakedModel(Identifier modelLocation){
        return Minecraft.getInstance().getModelManager().getStandaloneModel(TheatricalNeoForgeClient.EXTRA_MODELS.get(modelLocation));
    }
    public static String getModVersion() {
        ModFileInfo modFileById = FMLLoader.getCurrent().getLoadingModList().getModFileById(Theatrical.MOD_ID);
        if(modFileById != null) {
            return modFileById.versionString();
        }
        return "Unknown";
    }
}
