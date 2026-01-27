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
}
