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

    @Switch(name = "Enabled", description = "Display RNG meter on the screen", subcategory = "RNG Meter")
    public static boolean rngMeter = false;
    @Switch(name = "Background", subcategory = "RNG Meter")
    public static boolean RngBackground = false;
    @Switch(name = "Has Daemon Shard", description = "If you unlocked the Daemon Shards, enable this", subcategory = "RNG Meter")
    public static boolean hasDaemon = false;
    @Number(name = "Daemon shard level", min = 1, max = 10, subcategory = "RNG Meter")
    public static int daemonLevel = 1;

    @Color(name = "Start color", description = "Chroma start color", subcategory = "Chroma Settings")
    public static OneColor startColor = new OneColor(200, 200, 200);
    @Color(name = "End color", description = "Chroma end color", subcategory = "Chroma Settings")
    public static OneColor endColor = new OneColor(131, 131, 131);
    @Number(name = "Chroma speed", description = "Chroma speed", min = 0, max = 10, subcategory = "Chroma Settings")
    public static int chromaSpeed = 5;
    @Number(name = "Color offset", description = "Offset", min = 0, max = 10, subcategory = "Chroma Settings")
    public static int colorOffset = 5;

    private void jcolor() {
        // IDE 快速编辑颜色值 （似乎现在没有插件支持OneColor？）
        int sC = new java.awt.Color(200, 200, 200).getRGB();
        int eC = new java.awt.Color(122, 122, 122).getRGB();
    }
}
