package dev.imabad.theatrical.lighting;

import dev.imabad.theatrical.api.DynamicLightProvider;
import dev.imabad.theatrical.blockentities.light.BaseLightBlockEntity;
import dev.imabad.theatrical.compat.ModCompat;
import dev.imabad.theatrical.compat.ShimmerCompat;
import dev.imabad.theatrical.config.TheatricalConfig;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is heavily inspired by the system implemented in Ars-Nouvaeu found <a href="https://github.com/baileyholl/Ars-Nouveau/blob/main/src/main/java/com/hollingsworth/arsnouveau/common/light/">here</a>
 * This code is taken from LambDynamicLights, an MIT fabric mod: <a href="https://github.com/LambdAurora/LambDynamicLights">Github Link</a>
 *
 */
public class LightManager {
    private final static Set<DynamicLightProvider> dynamicLightSources = new HashSet<>();
    private final static ReentrantReadWriteLock lightSourcesLock = new ReentrantReadWriteLock();
    public static long lastUpdate = System.currentTimeMillis();
    public static List<Integer> jarHoldingEntityList = new ArrayList<>();
    public static int lastUpdateCount = 0;

    public static void addLightSource(DynamicLightProvider lightSource) {
        if (!lightSource.getLightWorld().isClientSide())
            return;
        if (!shouldUpdateDynamicLight())
            return;
        if (containsLightSource(lightSource))
            return;
        lightSourcesLock.writeLock().lock();
        dynamicLightSources.add(lightSource);
        if(ModCompat.SHIMMER){
            ShimmerCompat.addLight(lightSource);
        }
        lightSourcesLock.writeLock().unlock();
    }

    /**
     * Returns whether the light source is tracked or not.
     *
     * @param lightSource the light source to check
     * @return {@code true} if the light source is tracked, else {@code false}
     */
    public static boolean containsLightSource(@NotNull DynamicLightProvider lightSource) {
        if (!lightSource.getLightWorld().isClientSide())
            return false;

        boolean result;
        lightSourcesLock.readLock().lock();
        result = dynamicLightSources.contains(lightSource);
        lightSourcesLock.readLock().unlock();
        return result;
    }

    /**
     * Returns the number of dynamic light sources that currently emit lights.
     *
     * @return the number of dynamic light sources emitting light
     */
    public int getLightSourcesCount() {
        int result;

        lightSourcesLock.readLock().lock();
        result = dynamicLightSources.size();
        lightSourcesLock.readLock().unlock();

        return result;
    }

    /**
     * Removes the light source from the tracked light sources.
     *
     * @param lightSource the light source to remove
     */
    public static void removeLightSource(DynamicLightProvider lightSource) {
        lightSourcesLock.writeLock().lock();

        var sourceIterator = dynamicLightSources.iterator();
        DynamicLightProvider it;
        while (sourceIterator.hasNext()) {
            it = sourceIterator.next();
            if (it.equals(lightSource)) {
                sourceIterator.remove();
                if(ModCompat.SHIMMER){
                    ShimmerCompat.removeLight(lightSource.getOwnerPos());
                } else {
                    if (Minecraft.getInstance().level != null)
                        lightSource.scheduleTrackedChunksRebuild(Minecraft.getInstance().levelRenderer);
                }
                break;
            }
        }

        lightSourcesLock.writeLock().unlock();
    }

    /**
     * Clears light sources.
     */
    public static void clearLightSources() {
        lightSourcesLock.writeLock().lock();

        var sourceIterator = dynamicLightSources.iterator();
        DynamicLightProvider it;
        while (sourceIterator.hasNext()) {
            it = sourceIterator.next();
            sourceIterator.remove();
            if(ModCompat.SHIMMER){
                ShimmerCompat.removeLight(it.getOwnerPos());
            } else {
                if (Minecraft.getInstance().levelRenderer != null) {
                    if (it.getLightLuminance() > 0)
                        it.resetLight();
                    it.scheduleTrackedChunksRebuild(Minecraft.getInstance().levelRenderer);
                }
            }
        }
        LightManager.jarHoldingEntityList = new ArrayList<>();

        lightSourcesLock.writeLock().unlock();
    }


    /**
     * Schedules a chunk rebuild at the specified chunk position.
     *
     * @param renderer the renderer
     * @param chunkPos the chunk position
     */
    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, @NotNull BlockPos chunkPos) {
        scheduleChunkRebuild(renderer, chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
    }

    /**
     * Schedules a chunk rebuild at the specified chunk position.
     *
     * @param renderer the renderer
     * @param chunkPos the packed chunk position
     */
    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, long chunkPos) {
        scheduleChunkRebuild(renderer, BlockPos.getX(chunkPos), BlockPos.getY(chunkPos), BlockPos.getZ(chunkPos));
    }

    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, int x, int y, int z) {
        if (Minecraft.getInstance().level != null)
            renderer.setSectionDirty(x, y, z);
    }

    /**
     * Updates all light sources.
     *
     * @param renderer the renderer
     */
    public static void updateAll(LevelRenderer renderer) {
        long now = System.currentTimeMillis();


        lastUpdate = now;
        lastUpdateCount = 0;

        lightSourcesLock.readLock().lock();
        for (var lightSource : dynamicLightSources) {
            if (lightSource.updateDynamicLight(renderer)) {
                lastUpdateCount++;
            }
        }
        lightSourcesLock.readLock().unlock();

    }

    /**
     * Updates the tracked chunk sets.
     *
     * @param chunkPos the packed chunk position
     * @param old      the set of old chunk coordinates to remove this chunk from it
     * @param newPos   the set of new chunk coordinates to add this chunk to it
     */
    public static void updateTrackedChunks(@NotNull BlockPos chunkPos, @Nullable LongOpenHashSet old, @Nullable LongOpenHashSet newPos) {
        if (old != null || newPos != null) {
            long pos = chunkPos.asLong();
            if (old != null)
                old.remove(pos);
            if (newPos != null)
                newPos.add(pos);
        }
    }

    public static int getLightmapWithDynamicLight(@NotNull BlockPos pos, int lightmap) {
        return getLightmapWithDynamicLight(getDynamicLightLevel(pos), lightmap);
    }

    /**
     * Returns the lightmap with combined light levels.
     *
     * @param dynamicLightLevel the dynamic light level
     * @param lightmap          the vanilla lightmap coordinates
     * @return the modified lightmap coordinates
     */
    public static int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap) {
        if (dynamicLightLevel > 0) {
            // lightmap is (skyLevel << 20 | blockLevel << 4)

            // Get vanilla block light level.
            int blockLevel = getBlockLightNoPatch(lightmap);
            if (dynamicLightLevel > blockLevel) {
                // Equivalent to a << 4 bitshift with a little quirk: this one ensure more precision (more decimals are saved).
                int luminance = (int) (dynamicLightLevel * 16.0);
                lightmap &= 0xfff00000;
                lightmap |= luminance & 0x000fffff;
            }
        }

        return lightmap;
    }

    public static int getBlockLightNoPatch(int light) { // Reverts the forge patch to LightTexture.block
        return light >> 4 & '\uffff';
    }

    /**
     * Returns the dynamic light level at the specified position.
     *
     * @param pos the position
     * @return the dynamic light level at the specified position
     */
    public static double getDynamicLightLevel(@NotNull BlockPos pos) {
        double result = 0;
        lightSourcesLock.readLock().lock();
        for (var lightSource : dynamicLightSources) {
            result = maxDynamicLightLevel(pos, lightSource, result);
        }
        lightSourcesLock.readLock().unlock();

        return Mth.clamp(result, 0, 15);
    }

    private static final double MAX_RADIUS = 7.75;
    private static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;

    /**
     * Returns the dynamic light level generated by the light source at the specified position.
     *
     * @param pos               the position
     * @param lightSource       the light source
     * @param currentLightLevel the current surrounding dynamic light level
     * @return the dynamic light level at the specified position
     */
    public static double maxDynamicLightLevel(@NotNull BlockPos pos, @NotNull DynamicLightProvider lightSource, double currentLightLevel) {
        int luminance = lightSource.getLightLuminance();
        if (luminance > 0) {
            // Can't use Entity#squaredDistanceTo because of eye Y coordinate.
            Vector3f lightPos = lightSource.getLightPos();
            double dx = pos.getX() - lightPos.x + 0.5;
            double dy = pos.getY() - lightPos.y + 0.5;
            double dz = pos.getZ() - lightPos.z + 0.5;

            double distanceSquared = dx * dx + dy * dy + dz * dz;
            // 7.75 because else we would have to update more chunks and that's not a good idea.
            // 15 (max range for blocks) would be too much and a bit cheaty.
            if (distanceSquared <= MAX_RADIUS_SQUARED) {
                double multiplier = 1.0 - Math.sqrt(distanceSquared) / MAX_RADIUS;
                double lightLevel = multiplier * luminance;
                if (lightLevel > currentLightLevel) {
                    return lightLevel;
                }
            }
        }
        return currentLightLevel;
    }

    /**
     * Updates the dynamic lights tracking.
     *
     * @param lightSource the light source
     */
    public static void updateTracking(@NotNull DynamicLightProvider lightSource) {
        boolean enabled = lightSource.isLightEnabled();
        int luminance = lightSource.getLightLuminance();
        if (!enabled && luminance > 0) {
            lightSource.setLightEnabled(true);
        } else if (enabled && luminance < 1) {
            lightSource.setLightEnabled(false);
        }
    }

    public static boolean shouldUpdateDynamicLight() {
        return TheatricalConfig.INSTANCE.COMMON.shouldEmitLight;
    }

    public static boolean updateDynamicLight(BaseLightBlockEntity light, LevelRenderer renderer){
        int luminance = light.getLightLuminance();

        BlockPos emissionBlock = light.getEmissionBlock();
        if(!emissionBlock.equals(light.getPrevEmissionBlock()) || luminance != light.getPrevLuminance()){
            light.setPrevEmissionBlock(emissionBlock);
            light.setPrevLuminance(luminance);
            if(ModCompat.SHIMMER){
                ShimmerCompat.handleLightUpdate(light);
            } else {
                theatricalLightHandler(light, renderer, luminance, emissionBlock);
            }
            return true;
        } else if(ModCompat.SHIMMER){
            if(light.getPrevColour() != light.getLightColour() || light.getPrevSpread() != light.getLightSpread()){
                light.setPrevColour(light.getLightColour());
                light.setPrevSpread(light.getLightSpread());
                ShimmerCompat.handleLightUpdate(light);
            }
        }
        return false;
    }

    private static void theatricalLightHandler(BaseLightBlockEntity light, LevelRenderer renderer, int luminance, BlockPos emissionBlock) {
        var newPos = new LongOpenHashSet();

        if (luminance > 0) {
            var entityChunkPos = new ChunkPos(emissionBlock);
            var chunkPos = new BlockPos.MutableBlockPos(entityChunkPos.x, LambDynamicLightUtil.getSectionCoord(emissionBlock.getY()), entityChunkPos.z);

            LightManager.scheduleChunkRebuild(renderer, chunkPos);
            LightManager.updateTrackedChunks(chunkPos, light.getTrackedLitChunkPos(), newPos);

            var directionX = (emissionBlock.getX() & 15) >= 8 ? Direction.EAST : Direction.WEST;
            var directionY = (emissionBlock.getY() & 15) >= 8 ? Direction.UP : Direction.DOWN;
            var directionZ = (emissionBlock.getZ() & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;

            for (int i = 0; i < 7; i++) {
                if (i % 4 == 0) {
                    chunkPos.move(directionX); // X
                } else if (i % 4 == 1) {
                    chunkPos.move(directionZ); // XZ
                } else if (i % 4 == 2) {
                    chunkPos.move(directionX.getOpposite()); // Z
                } else {
                    chunkPos.move(directionZ.getOpposite()); // origin
                    chunkPos.move(directionY); // Y
                }
                LightManager.scheduleChunkRebuild(renderer, chunkPos);
                LightManager.updateTrackedChunks(chunkPos, light.getTrackedLitChunkPos(), newPos);
            }
        }
        // Schedules the rebuild of removed chunks.
        light.scheduleTrackedChunksRebuild(renderer);
        // Update tracked lit chunks.
        light.setTrackedLitChunkPos(newPos);
    }
}
