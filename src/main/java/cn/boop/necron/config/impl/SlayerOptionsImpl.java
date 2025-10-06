package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class SlayerOptionsImpl extends ModConfig {
    public SlayerOptionsImpl() {
        super("Slayer", "necron/slayers.json");
        initialize();
    }

//    @Switch(name = "Enabled", description = "Slayer helper")
//    public static boolean slayer = false;
    @Switch(name = "Kill Time", description = "Display the time to kill the slayer boss")
    public static boolean killTime = false;
    @Switch(name = "Soulcry", description = "Auto use Soulcry ability", subcategory = "Enderman Slayer")
    public static boolean voidgloom = false;
    @Switch(name = "Auto Heal", description = "Auto use Healing Wands", subcategory = "Enderman Slayer")
    public static boolean heal = false;
}
