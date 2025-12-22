package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class AutoLeapOptionsImpl extends ModConfig {
    public AutoLeapOptionsImpl() {
        super("Auto Leap", "necron/autoleap.json");
        initialize();
    }

    @Switch(name = "Auto Leap", description = "Auto leap to players based on predefined rules", subcategory = "Auto Leap")
    public static boolean autoLeap = false;
    @Switch(name = "Auto Leap on Floor 7", description = "Enable auto leaping during F7/M7 boss fight", subcategory = "Auto Leap")
    public static boolean autoLeapF7 = false;
}
