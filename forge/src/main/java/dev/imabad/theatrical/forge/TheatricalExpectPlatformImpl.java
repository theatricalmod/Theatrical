package dev.imabad.theatrical.forge;

import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.blockentities.CableBlockEntity;
import dev.imabad.theatrical.forge.blockentity.CableBlockEntityForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    public static BlockEntityType.BlockEntitySupplier<CableBlockEntity> getCableBlockEntity() {
        return CableBlockEntityForge::new;
    }

}
