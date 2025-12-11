package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class NametagsOptionsImpl extends ModConfig {
    public NametagsOptionsImpl() {
        super("Nametags", "necron/nametags.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Display nametags of players")
    public static boolean nametags = false;

    @Switch(name = "Show Distance", description = "Show distance to player", subcategory = "Nametags")
    public static boolean renderDistance = true;
    @Switch(name = "Background", description = "Render nametag background", subcategory = "Nametags")
    public static boolean renderBg = true;
    @Switch(name = "Shadow Text", description = "Display nametags as shadow text", subcategory = "Nametags")
    public static boolean shadowText = true;
    @Switch(name = "Teammate ESP", description = "Draw an ESP box around dungeon teammates", subcategory = "Nametags")
    public static boolean teammateESP = false;
    @Switch(name = "Force SkyBlock", description = "Display nametags outside of SkyBlock", subcategory = "Nametags")
    public static boolean forceSkyBlock = false;
}
