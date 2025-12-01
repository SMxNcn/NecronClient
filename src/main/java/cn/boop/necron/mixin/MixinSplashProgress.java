package cn.boop.necron.mixin;

import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = {SplashProgress.class}, remap = false)
public class MixinSplashProgress {
    /**
     * @author Boop
     * @reason NecronClient
     * @see cn.boop.necron.gui.LoadingScreen
     */
    @Overwrite
    public static void start() {
    }
}
