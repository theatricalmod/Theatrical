package dev.imabad.theatrical.compat;

import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import dev.imabad.theatrical.api.DynamicLightProvider;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ShimmerCompat {

    private static final Map<BlockPos, ColorPointLight> pos2PointLight = new ConcurrentHashMap<>();
    private final static ReentrantReadWriteLock lightSourcesLock = new ReentrantReadWriteLock();

    public static void setup(){}

    public static void addLight(DynamicLightProvider dynamicLightProvider){
        if(!ModCompat.SHIMMER) return;
        lightSourcesLock.writeLock().lock();
        ColorPointLight light = LightManager.INSTANCE.addLight(
                dynamicLightProvider.getLightPos(),
                dynamicLightProvider.getLightColour(),
                (float) dynamicLightProvider.getLightRadius()
        );

        if(light != null){
            pos2PointLight.put(dynamicLightProvider.getOwnerPos(), light);
        }
        lightSourcesLock.writeLock().unlock();
    }

    public static void removeLight(BlockPos fixturePos){
        if(!ModCompat.SHIMMER) return;
        lightSourcesLock.writeLock().lock();
        if(pos2PointLight.containsKey(fixturePos)){
            pos2PointLight.get(fixturePos).remove();
        }
        pos2PointLight.remove(fixturePos);
        lightSourcesLock.writeLock().unlock();
    }

    public static void worldClose(){
        if(!ModCompat.SHIMMER) return;
        lightSourcesLock.writeLock().lock();
        pos2PointLight.forEach((blockPos, colorPointLight) -> colorPointLight.remove());
        pos2PointLight.clear();
        lightSourcesLock.writeLock().unlock();
    }

    public static void handleLightUpdate(BaseLightBlockEntity light){
        if(!pos2PointLight.containsKey(light.getBlockPos())) return;

        ColorPointLight oldLight = pos2PointLight.get(light.getBlockPos());
        oldLight.remove();
        pos2PointLight.remove(light.getBlockPos());

        ColorPointLight newLight = LightManager.INSTANCE.addLight(
                light.getLightPos(),
                light.getLightColour(),
                (float) light.getLightRadius()
        );

        if(newLight != null){
            pos2PointLight.put(light.getBlockPos(), newLight);
        }
    }
}