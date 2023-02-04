package dev.imabad.theatrical.client;

import dev.imabad.theatrical.TheatricalExpectPlatform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BakedModelCache {

    private final Map<ResourceLocation, BakedModel> bakedModelMap = new HashMap<>();

    public BakedModelCache(){

    }

    public void onReload(){

    }

    public BakedModel getOrFind(ResourceLocation modelLocation) {
        return bakedModelMap.computeIfAbsent(modelLocation, TheatricalExpectPlatform::getBakedModel);
    }

}
