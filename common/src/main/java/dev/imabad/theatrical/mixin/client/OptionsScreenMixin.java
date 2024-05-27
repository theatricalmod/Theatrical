package dev.imabad.theatrical.mixin.client;

import dev.imabad.theatrical.client.gui.screen.ArtNetConfigurationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
@Debug(export = true)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Shadow protected abstract Button openScreenButton(Component message, Supplier<Screen> screenSupplier);

    @Inject(method = "init()V", at = @At(value = "INVOKE", target="Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onCreatePauseMenu(CallbackInfo ci, GridLayout gridLayout, GridLayout.RowHelper rowHelper){
        rowHelper.addChild(this.openScreenButton(Component.translatable("button.artnetconfig"), () -> new ArtNetConfigurationScreen((OptionsScreen)(Object) this)));
    }

}
