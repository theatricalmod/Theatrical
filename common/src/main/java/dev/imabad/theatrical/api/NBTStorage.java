package dev.imabad.theatrical.api;

import net.minecraft.nbt.CompoundTag;

public interface NBTStorage {
    void write(CompoundTag compoundTag);

    void read(CompoundTag compoundTag);
}
