package cn.boop.necron.mixin;

import cc.polyfrost.oneconfig.gui.pages.CreditsPage;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreditsPage.class)
public class MixinCreditsPage {

    @Inject(method = "draw", at = @At("HEAD"), remap = false)
    private void draw(long vg, int x, int y, InputHandler inputHandler, CallbackInfo ci) {
        final NanoVGHelper nanoVGHelper = NanoVGHelper.INSTANCE;
        nanoVGHelper.drawText(vg, "Necron Client & OneConfig 兼容层", x + 20, y + 470, -1, 24, Fonts.SEMIBOLD);
        nanoVGHelper.drawText(vg, " - 用于Hypixel SkyBlock的QoL模组", x + 20, y + 495, -1, 12, Fonts.REGULAR);
        nanoVGHelper.drawText(vg, " - 通过Mixin注入来支持中文字体渲染", x + 20, y + 510, -1, 12, Fonts.REGULAR);
        nanoVGHelper.drawText(vg, " - 仅使用OneConfig作为模组配置库", x + 20, y + 525, -1, 12, Fonts.REGULAR);
    }
}
