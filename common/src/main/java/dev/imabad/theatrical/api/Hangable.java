package dev.imabad.theatrical.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public interface Hangable {
    boolean isHanging(LevelReader levelReader, BlockPos pos);
}
