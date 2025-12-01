package cn.boop.necron.mixin;

import cn.boop.necron.gui.LoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public GuiScreen currentScreen;

    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    private void replaceSplashScreen(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createDisplay()V", shift = At.Shift.AFTER)})
    private void splash(CallbackInfo ci) {
        LoadingScreen.start();
        LoadingScreen.setProgress(0);
    }

    @Inject(method = {"startGame"}, at = {@At("RETURN")})
    private void endLoad(CallbackInfo ci) {
        if (LoadingScreen.isFinished()) return;
        LoadingScreen.finish();
    }
}
