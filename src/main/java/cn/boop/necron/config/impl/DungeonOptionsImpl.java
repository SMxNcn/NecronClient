package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cn.boop.necron.config.ModConfig;

public class DungeonOptionsImpl extends ModConfig {
    public DungeonOptionsImpl() {
        super("Dungeon Options", "necron/dungeons.json");
        initialize();

        addDependency("daemonLevel", "hasDaemon");
    }

    @Switch(name = "Enable", description = "Enable Reroll Protector", subcategory = "Reroll Protector")
    public static boolean reroll = true;
    @Switch(name = "Protect Reroll", description = "Prevent clicking Reroll button on RNG items", subcategory = "Reroll Protector")
    public static boolean rerollProtect = true;
    @Switch(name = "Send RNG to party", description = "Send your RNG to teammates", subcategory = "Reroll Protector")
    public static boolean sendToParty = true;

    @Switch(name = "P5 Waypoints", subcategory = "Dungeon")
    public static boolean m7Waypoints = false;
    @Switch(name = "Only P5", subcategory = "Dungeon")
    public static boolean onlyP5 = false;
    @Switch(name = "Bat ESP", subcategory = "Dungeon")
    public static boolean batESP = false;
    @Switch(name = "Wither ESP", subcategory = "Dungeon")
    public static boolean witherESP = false;
    @Color(name = "ESP Color", description = "Color of dungeon ESP", subcategory = "Dungeon")
    public static OneColor espColor = new OneColor(255, 182, 43);
}
