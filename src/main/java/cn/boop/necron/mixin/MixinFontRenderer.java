package cn.boop.necron.mixin;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 修复了SkyHanni颜色格式错误导致的IllegalStateException崩溃问题
 @see <a href="https://github.com/hannibal002/SkyHanni/blob/beta/src/main/java/at/hannibal2/skyhanni/mixins/transformers/ExtendedColorPatch.java#L92">SkyHanni相关实现</a>
 */

@Mixin(FontRenderer.class)
public class MixinFontRenderer {
    @Unique
    private static long necronClient$lastWarningTime = 0;
    @Unique
    private static final long necronClient$WARNING_COOLDOWN = 20_000L;

    @Inject(method = "renderStringAtPos", at = @At("HEAD"), cancellable = true)
    private void validateSkyHanniColorFormat(String text, boolean shadow, CallbackInfo ci) {
        if (text != null && text.contains("§#") && text.contains("§/")) {
            if (!necronClient$isValidSkyHanniColorFormat(text)) {
                necronClient$handleInvalidColorFormat(text);
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean necronClient$isValidSkyHanniColorFormat(String text) {
        int startIndex = 0;
        while ((startIndex = text.indexOf("§#", startIndex)) != -1) {
            int endIndex = text.indexOf("§/", startIndex);
            if (endIndex == -1) {
                break;
            }

            if (endIndex <= startIndex + 2) {
                startIndex++;
                continue;
            }

            String colorSection = text.substring(startIndex + 2, endIndex);
            if (!RenderUtils.isValidSHiColorPattern(colorSection)) {
                if (!colorSection.isEmpty()) return false;
            }

            startIndex = endIndex + 2;
        }
        return true;
    }

    @Unique
    private void necronClient$handleInvalidColorFormat(String invalidText) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - necronClient$lastWarningTime >= necronClient$WARNING_COOLDOWN) {
            necronClient$lastWarningTime = currentTime;
            Necron.LOGGER.warn("§cInvalid SkyHanni color format! Text: {}", invalidText);
        }
    }
}
