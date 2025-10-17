package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cn.boop.necron.config.ModConfig;

public class ClientHUDOptionsImpl extends ModConfig {
    public ClientHUDOptionsImpl() {
        super("GUI Options", "necron/guioptions.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Display active modules on the screen", subcategory = "Module List")
    public static boolean moduleList = false;

    @Color(name = "Start color", description = "Chroma start color", subcategory = "Chroma Settings")
    public static OneColor startColor = new OneColor(200, 200, 200);
    @Color(name = "End color", description = "Chroma end color", subcategory = "Chroma Settings")
    public static OneColor endColor = new OneColor(131, 131, 131);
    @Number(name = "Chroma speed", description = "Chroma speed", min = 0, max = 10, subcategory = "Chroma Settings")
    public static int chromaSpeed = 5;
    @Number(name = "Color offset", description = "Offset", min = 0, max = 10, subcategory = "Chroma Settings")
    public static int colorOffset = 5;
}
