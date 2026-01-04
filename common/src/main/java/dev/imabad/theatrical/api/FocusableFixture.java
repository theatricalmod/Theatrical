package dev.imabad.theatrical.api;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface FocusableFixture {

    void setTrackingEntity(@Nullable Entity entity);
    @Nullable
    Entity getTrackingEntity();

}
