package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cn.boop.necron.config.ModConfig;

public class WaypointOptionsImpl extends ModConfig {
    public WaypointOptionsImpl() {
        super("Waypoints", "necron/waypoints.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Custom waypoint")
    public static boolean waypoints = false;
    @Color(name = "Box Color")
    public static OneColor boxColor = new OneColor(163, 212, 244, 255); // #51ACE0
    @Color(name = "Line Color")
    public static OneColor lineColor = new OneColor(58, 160, 239, 255); // #3BA0ED
    @Color(name = "Text Color")
    public static OneColor textColor = new OneColor(79, 170, 225, 255); // #52ADDE
}
