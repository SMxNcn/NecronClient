package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class RouterOptionsImpl extends ModConfig {
    public RouterOptionsImpl() {
        super("Etherwarp Router", "necron/router.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Etherwarp route (WIP)")
    public static boolean router = false;
    @Switch(name = "Loop", description = "Enable loop mode for current waypoints", subcategory = "Router")
    public static boolean isLoop = false;
    @Switch(name = "Dev Message", description = "Display debug message while using Etherwarp Router", subcategory = "Router")
    public static boolean devMsg = false;
    @Switch(name = "Pre-aiming", description = "Enable pre-aiming to next waypoint before you left click", subcategory = "Router")
    public static boolean preAiming = false;
    @Switch(name = "Always sneak", description = "Always sneak while using Etherwarp Router", subcategory = "Router")
    public static boolean alwaysSneak = false;
}
