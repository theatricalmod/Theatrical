package dev.imabad.theatrical.compat;

import com.lowdragmc.shimmer.client.light.ColorPointLight;
import com.lowdragmc.shimmer.client.light.LightManager;
import dev.imabad.theatrical.api.DynamicLightProvider;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
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
        ColorPointLight light = LightManager.INSTANCE.addLight(dynamicLightProvider.getLightPos(), dynamicLightProvider.getLightColour(), 8);

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
        pos2PointLight.forEach((blockPos, colorPointLight) -> {
            colorPointLight.remove();
        });
        pos2PointLight.clear();
        lightSourcesLock.writeLock().unlock();
    }

    public static void handleLightUpdate(BaseLightBlockEntity light){
        if(!pos2PointLight.containsKey(light.getBlockPos())) return;
        ColorPointLight colorPointLight = pos2PointLight.get(light.getBlockPos());
        Vector3f lightPos = light.getLightPos();
        colorPointLight.setPos(lightPos.x, lightPos.y, lightPos.z);
        colorPointLight.setColor(light.getLightColour());
        colorPointLight.setEnable(light.isLightEnabled());
        colorPointLight.update();
    }
}
