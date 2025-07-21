package dev.imabad.theatrical.api;

import dev.imabad.theatrical.lighting.LightManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public interface DynamicLightProvider {

    BlockPos getOwnerPos();
    Vector3f getLightPos();
    Level getLightWorld();
    default boolean isLightEnabled() {
        return LightManager.containsLightSource(this);
    }
    default double getLightRadius() {
        return 8.5;
    }
    default void setLightEnabled(boolean enabled) {
        resetLight();
        if(enabled){
            LightManager.addLightSource(this);
        } else {
            LightManager.removeLightSource(this);
        }
    }
    void resetLight();
    int getLightLuminance();
    void lightTick();
    boolean shouldUpdateLight();
    boolean updateDynamicLight(LevelRenderer renderer);
    void scheduleTrackedChunksRebuild(LevelRenderer renderer);
    int getLightColour();
    int getLightSpread();
}
