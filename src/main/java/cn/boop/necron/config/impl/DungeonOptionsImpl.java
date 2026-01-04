package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.InfoType;
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
    @DualOption(name = "RNG Message type", left = "Normal", right = "Meme", description = "Message type of RNG", subcategory = "Reroll Protector")
    public static boolean memeRng = false;

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
    @Switch(name = "Mask Notifier", description = "Enable Mask/Phoenix used message", subcategory = "Dungeon")
    public static boolean maskNotifier = false;
    @Text(name = "Spirit Text", description = "Message when Spirit Mask used", subcategory = "Dungeon")
    public static String spiritText = "";
    @Text(name = "Bonzo Text", description = "Message when Spirit Mask used", subcategory = "Dungeon")
    public static String bonzoText = "";
    @Text(name = "Phoenix Text", description = "Message when Spirit Mask used", subcategory = "Dungeon")
    public static String phoenixText = "";

    @Switch(name = "Enabled", description = "Enable i4", subcategory = "Auto i4")
    public static boolean autoI4 = false;
    @Info(text = "Please use Bonzo's Mask First!", type = InfoType.WARNING, subcategory = "Auto i4")
    public static boolean ignore;
    @Number(name = "Rod Slot", min = 1, max = 8, description = "Slot of Fishing Rod", subcategory = "Auto i4")
    public static int rodSlot = 4;
    @Number(name = "Leap Slot", min = 1, max = 8, description = "Slot of Leap Item", subcategory = "Auto i4")
    public static int leapSlot = 8;
}
