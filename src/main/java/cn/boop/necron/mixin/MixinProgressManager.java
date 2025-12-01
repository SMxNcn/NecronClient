package cn.boop.necron.mixin;

import cn.boop.necron.gui.LoadingScreen;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressManager.class)
public class MixinProgressManager {
    @Inject(method = "pop", at = @At("HEAD"), remap = false)
    private static void onProgressPop(ProgressManager.ProgressBar bar, CallbackInfo ci) {
        if (LoadingScreen.progress < LoadingScreen.MAX_PROGRESS) {
            LoadingScreen.progress += 3;
        }
    }
}