package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class AutoClickerOptionsImpl extends ModConfig {
    public AutoClickerOptionsImpl() {
        super("Auto Clicker", "necron/autoclicker.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Enable the auto clicker module")
    public static boolean autoClicker = false;

    @Switch(name = "Left Click", description = "Automatically perform left clicks", subcategory = "Auto Clicker")
    public static boolean leftClick = true;
    @Switch(name = "Right Click", description = "Automatically perform right clicks", subcategory = "Auto Clicker")
    public static boolean rightClick = true;
    @Switch(name = "Weapon Only", description = "Only allow auto clicking when holding a sword", subcategory = "Auto Clicker")
    public static boolean weaponsOnly = true;

    @Number(name = "Min CPS", min = 1, max = 20, subcategory = "Left Click")
    public static int minLCPS = 10;
    @Number(name = "Max CPS", min = 2, max = 20, subcategory = "Left Click")
    public static int maxLCPS = 10;

    @Number(name = "Min CPS", min = 1, max = 20, subcategory = "Right Click")
    public static int minRCPS = 10;
    @Number(name = "Max CPS", min = 2, max = 20, subcategory = "Right Click")
    public static int maxRCPS = 10;
}
