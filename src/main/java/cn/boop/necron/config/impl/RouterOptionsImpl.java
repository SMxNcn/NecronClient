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
    @Switch(name = "Loop", description = "Enable loop mode for current waypoints")
    public static boolean isLoop = false;
    @Switch(name = "Dev Message", description = "Display debug message while using Etherwarp Router")
    public static boolean devMsg = false;
}
