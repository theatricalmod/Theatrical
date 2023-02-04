package dev.imabad.theatrical.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

public interface Support {
    float[] getHookTransforms(LevelReader levelReader, BlockPos pos, Direction facing);
}
