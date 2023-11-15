package dev.imabad.theatrical.mixin;


import dev.imabad.theatrical.util.ExtServerPlayerGameMode;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerGameMode implements ExtServerPlayerGameMode {

    private boolean captureSentBlockEntities = false;
    private boolean capturedBlockEntity = false;

    @Override
    public boolean hasCapturedBlockEntity() {
        return capturedBlockEntity;
    }

    @Override
    public void setCapturedBlockEntity(boolean hasCapturedBlockEntity) {
        capturedBlockEntity = hasCapturedBlockEntity;
    }

    @Override
    public boolean shouldCaptureSentBlockEntities() {
        return captureSentBlockEntities;
    }

    @Override
    public void setCaptureSentBlockEntities(boolean shouldCaptureSentBlockEntities) {
        captureSentBlockEntities = shouldCaptureSentBlockEntities;
    }
}
