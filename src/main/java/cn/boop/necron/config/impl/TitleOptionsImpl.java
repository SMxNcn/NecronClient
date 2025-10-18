package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cn.boop.necron.config.ModConfig;

public class TitleOptionsImpl extends ModConfig {
    public TitleOptionsImpl() {
        super("Title Manager", "necron/titlemanager.json");
        initialize();

        addDependency("titleText", "customTitle");
        addDependency("showPlayerName", "customType");
        addDependency("showLocation", "customType");
        addDependency("showTips", "customType");
        addDependency("customPrefix", "customType");
        addDependency("prefixText", "customPrefix");
    }

    @Switch(name = "Change Title", description = "Use custom window title")
    public static boolean title = true;

    @DualOption(name = "Title type", left = "Default", right = "Custom")
    public static boolean customType = false;

    @Checkbox(name = "Player Name", category = "Custom")
    public static boolean showPlayerName = true;
    @Checkbox(name = "Location", category = "Custom")
    public static boolean showLocation = true;
    @Checkbox(name = "Tips", category = "Custom")
    public static boolean showTips = true;
    @Checkbox(name = "Custom prefix", category = "Custom")
    public static boolean customPrefix = true;
    @Text(name = "Prefix Text", placeholder = "Text here", category = "Custom")
    public static String prefixText = "";

    @Switch(name = "Change Icon", description = "Use Custom window icon")
    public static boolean icon = false;
    @Switch(name = "Use your title text", size = 2)
    public static boolean customTitle = false;
    @Text(name = "Title Text", placeholder = "Text here")
    public static String titleText = "";
}
