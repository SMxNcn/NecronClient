package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cn.boop.necron.config.ModConfig;

public class GUIOptionsImpl extends ModConfig {
    public GUIOptionsImpl() {
        super("GUI Options", "necron/guioptions.json");
        initialize();

        addDependency("redNumbers", "customSb");
        addDependency("shadowText", "customSb");
        addDependency("bgColor", "customSb");
        addDependency("xPadding", "customSb");
        addDependency("yPadding", "customSb");
        addDependency("cornerRadius", "customSb");
        addDependency("customIp", "customSb");
        addDependency("ignore1", "customSb");
        addDependency("alignCenter", "customSb");
        addDependency("clientName", "customSb");
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

    @Switch(name = "Enabled", description = "Display RNG meter on the screen", subcategory = "RNG Meter")
    public static boolean rngMeter = false;
    @Switch(name = "Background", subcategory = "RNG Meter")
    public static boolean RngBackground = false;
    @Switch(name = "Has Daemon Shard", description = "If you have unlocked the Daemon Shards, enable this", subcategory = "RNG Meter")
    public static boolean hasDaemon = false;
    @Number(name = "Daemon shard level", min = 1, max = 10, subcategory = "RNG Meter")
    public static int daemonLevel = 1;

    @Switch(name = "Enabled", description = "Display custom scoreboard", subcategory = "Scoreboard")
    public static boolean customSb = false;
    @Switch(name = "Show red numbers", description = "Show red numbers", subcategory = "Scoreboard")
    public static boolean redNumbers = true;
    @Switch(name = "Use Shadow Text", description = "Use shadow text", subcategory = "Scoreboard")
    public static boolean shadowText = false;
    @Color(name = "Background Color", description = "Background color of the scoreboard", subcategory = "Scoreboard")
    public static OneColor sbBgColor = new OneColor(0, 0, 0, 77);
    @Slider(name = "X-Padding", description = "X-Padding of the scoreboard", min = 0, max = 5, subcategory = "Scoreboard")
    public static int xPadding = 2;
    @Slider(name = "Y-Padding", description = "Y-Padding of the scoreboard", min = 0, max = 5, subcategory = "Scoreboard")
    public static int yPadding = 2;
    @Slider(name = "Corner Radius", description = "Corner radius of the scoreboard", min = 0, max = 10, subcategory = "Scoreboard")
    public static int sbCornerRadius = 4;
    @Text(name = "Custom IP", description = "Custom IP-Line of the scoreboard", subcategory = "Scoreboard")
    public static String customIp = "";
    @Info(text = "'&' or '§' for Minecraft Color Codes", type = InfoType.INFO, subcategory = "Scoreboard")
    private static boolean ignore1;
    @Switch(name = "Align Center", description = "Align the Custom IP to the center of the scoreboard", subcategory = "Scoreboard")
    public static boolean alignCenter = false;
    @Switch(name = "Show Client Name", description = "Show the client name", subcategory = "Scoreboard")
    public static boolean clientName = false;
}
