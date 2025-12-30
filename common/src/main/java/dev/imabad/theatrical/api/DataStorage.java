package dev.imabad.theatrical.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface DataStorage {
    void write(ValueOutput out);

    void read(ValueInput in);
}
