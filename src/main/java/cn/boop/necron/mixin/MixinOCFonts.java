package cn.boop.necron.mixin;

import cc.polyfrost.oneconfig.renderer.font.Font;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cn.boop.necron.config.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(Fonts.class)
public class MixinOCFonts {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;)Lcc/polyfrost/oneconfig/renderer/font/Font;"), remap = false)
    private static Font redirectFontConstructor(String name, String path) {
        String configFontPath = FontManager.getFontPath(new File(path).getName());
        return configFontPath != null ? new Font(name, configFontPath) : new Font(name, path);
    }
}
