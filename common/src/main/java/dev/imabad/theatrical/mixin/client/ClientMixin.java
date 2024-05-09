package dev.imabad.theatrical.mixin.client;

import dev.imabad.theatrical.lighting.LightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.vehicle.Minecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This is heavily inspired by the system implemented in Ars-Nouvaeu found <a href="https://github.com/baileyholl/Ars-Nouveau/blob/main/src/main/java/com/hollingsworth/arsnouveau/common/light/">here</a>
 * This code is taken from LambDynamicLights, an MIT fabric mod: <a href="https://github.com/LambdAurora/LambDynamicLights">Github Link</a>
 */
@Mixin(Minecraft.class)
public class ClientMixin {
    @Inject(method = "updateLevelInEngines", at= @At("HEAD"))
    private void onSetWorld(ClientLevel level, CallbackInfo ci){
        LightManager.clearLightSources();
    }
}
