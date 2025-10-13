package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class AutoPathOptionsImpl extends ModConfig {
    public AutoPathOptionsImpl() {
        super("AutoPath", "necron/autopath.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "A* Path finder")
    public static boolean autoPath = false;
    @Switch(name = "Follow", description = "Follow the path", subcategory = "Path")
    public static boolean follow = false;
    @Switch(name = "Sprint", description = "Sprint when following", subcategory = "Path")
    public static boolean sprint = false;
    @Switch(name = "Render path", description = "Render the result path", subcategory = "Path")
    public static boolean renderPath = true;
    @Switch(name = "Render nodes", description = "Render nodes number on the path", subcategory = "Path")
    public static boolean renderNodes = false;
}
