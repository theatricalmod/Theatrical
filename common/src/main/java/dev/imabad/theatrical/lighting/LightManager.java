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
        if (ModCompat.SHIMMER) {
            ShimmerCompat.addLight(lightSource);
        }
        lightSourcesLock.writeLock().unlock();
    }

    public static boolean containsLightSource(@NotNull DynamicLightProvider lightSource) {
        if (!lightSource.getLightWorld().isClientSide())
            return false;

        boolean result;
        lightSourcesLock.readLock().lock();
        result = dynamicLightSources.contains(lightSource);
        lightSourcesLock.readLock().unlock();
        return result;
    }

    public int getLightSourcesCount() {
        int result;

        lightSourcesLock.readLock().lock();
        result = dynamicLightSources.size();
        lightSourcesLock.readLock().unlock();

        return result;
    }

    public static void removeLightSource(DynamicLightProvider lightSource) {
        lightSourcesLock.writeLock().lock();

        var sourceIterator = dynamicLightSources.iterator();
        DynamicLightProvider it;
        while (sourceIterator.hasNext()) {
            it = sourceIterator.next();
            if (it.equals(lightSource)) {
                sourceIterator.remove();
                if (ModCompat.SHIMMER) {
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

    public static void clearLightSources() {
        lightSourcesLock.writeLock().lock();

        var sourceIterator = dynamicLightSources.iterator();
        DynamicLightProvider it;
        while (sourceIterator.hasNext()) {
            it = sourceIterator.next();
            sourceIterator.remove();
            if (ModCompat.SHIMMER) {
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

    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, @NotNull BlockPos chunkPos) {
        scheduleChunkRebuild(renderer, chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
    }

    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, long chunkPos) {
        scheduleChunkRebuild(renderer, BlockPos.getX(chunkPos), BlockPos.getY(chunkPos), BlockPos.getZ(chunkPos));
    }

    public static void scheduleChunkRebuild(@NotNull LevelRenderer renderer, int x, int y, int z) {
        if (Minecraft.getInstance().level != null)
            renderer.setSectionDirty(x, y, z);
    }

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

    public static int getLightmapWithDynamicLight(double dynamicLightLevel, int lightmap) {
        if (dynamicLightLevel > 0) {
            int blockLevel = getBlockLightNoPatch(lightmap);
            if (dynamicLightLevel > blockLevel) {
                int luminance = (int) (dynamicLightLevel * 16.0);
                lightmap &= 0xfff00000;
                lightmap |= luminance & 0x000fffff;
            }
        }
        return lightmap;
    }

    public static int getBlockLightNoPatch(int light) {
        return light >> 4 & '\uffff';
    }

    public static double getDynamicLightLevel(@NotNull BlockPos pos) {
        double result = 0;
        lightSourcesLock.readLock().lock();
        for (var lightSource : dynamicLightSources) {
            result = maxDynamicLightLevel(pos, lightSource, result);
        }
        lightSourcesLock.readLock().unlock();

        return Mth.clamp(result, 0, 15);
    }

    public static double maxDynamicLightLevel(@NotNull BlockPos pos, @NotNull DynamicLightProvider lightSource, double currentLightLevel) {
        int luminance = lightSource.getLightLuminance();
        if (luminance > 0) {
            Vector3f lightPos = lightSource.getLightPos();
            double dx = pos.getX() - lightPos.x + 0.5;
            double dy = pos.getY() - lightPos.y + 0.5;
            double dz = pos.getZ() - lightPos.z + 0.5;

            double distanceSquared = dx * dx + dy * dy + dz * dz;

            double radius = lightSource.getLightRadius();
            double radiusSquared = radius * radius;

            if (distanceSquared <= radiusSquared) {
                double multiplier = 1.0 - Math.sqrt(distanceSquared) / radius;
                double lightLevel = multiplier * luminance;
                if (lightLevel > currentLightLevel) {
                    return lightLevel;
                }
            }
        }
        return currentLightLevel;
    }

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
                    chunkPos.move(directionX);
                } else if (i % 4 == 1) {
                    chunkPos.move(directionZ);
                } else if (i % 4 == 2) {
                    chunkPos.move(directionX.getOpposite());
                } else {
                    chunkPos.move(directionZ.getOpposite());
                    chunkPos.move(directionY);
                }
                LightManager.scheduleChunkRebuild(renderer, chunkPos);
                LightManager.updateTrackedChunks(chunkPos, light.getTrackedLitChunkPos(), newPos);
            }
        }
        light.scheduleTrackedChunksRebuild(renderer);
        light.setTrackedLitChunkPos(newPos);
    }
}
