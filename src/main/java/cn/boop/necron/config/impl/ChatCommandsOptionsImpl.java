package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class ChatCommandsOptionsImpl extends ModConfig {
    public ChatCommandsOptionsImpl() {
        super("Chat Commands", "necron/chatcommands.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Party commands in Hypixel")
    public static boolean chatCommands = true;
    @Checkbox(name = "Help", subcategory = "Commands")
    public static boolean help = true;
    @Checkbox(name = "Location", subcategory = "Commands")
    public static boolean location = true;
    @Checkbox(name = "Meow", subcategory = "Commands")
    public static boolean meow = true;
    @Checkbox(name = "Party Nuker", subcategory = "Commands")
    public static boolean nuke = false;
    @Checkbox(name = "Daily luck", subcategory = "Commands")
    public static boolean rng = true;
    @Checkbox(name = "M7 Drops Roll", subcategory = "Commands")
    public static boolean roll = true;
    @Checkbox(name = "NTMSB?", subcategory = "Commands")
    public static boolean sb = true;
    @Checkbox(name = "Useful Tips", subcategory = "Commands")
    public static boolean tips = true;
    @Checkbox(name = "Zako~", subcategory = "Commands")
    public static boolean zako = true;
}
