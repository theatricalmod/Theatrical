package dev.imabad.theatrical.fabric;

import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;

public class TheatricalExpectPlatformImpl {
    /**
     * This is our actual method to {@link TheatricalExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static BakedModel getBakedModel(ResourceLocation modelLocation){
        return BakedModelManagerHelper.getModel(Minecraft.getInstance().getModelManager(), modelLocation);
    }

}
