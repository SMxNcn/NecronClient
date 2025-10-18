package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Switch;
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

    @Switch(name = "Enabled", description = "Display RNG meter on the screen", subcategory = "RNG Meter")
    public static boolean rngMeter = false;
    @Switch(name = "Background", subcategory = "RNG Meter")
    public static boolean RngBackground = false;
    @Switch(name = "Has Daemon Shard", description = "If you have unlocked the Daemon Shards, enable this", subcategory = "RNG Meter")
    public static boolean hasDaemon = false;
    @Number(name = "Daemon shard level", min = 1, max = 10, subcategory = "RNG Meter")
    public static int daemonLevel = 1;
}
