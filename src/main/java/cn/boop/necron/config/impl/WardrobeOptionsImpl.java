package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class WardrobeOptionsImpl extends ModConfig {
    public WardrobeOptionsImpl() {
        super("Wardrobe", "necron/wardrobe.json");
        initialize();

        addDependency("blockInDungeon", "unEquip");
    }

    @Switch(name = "Enabled", description = "Wardrobe QoL")
    public static boolean wardrobe = false;
    @Switch(name = "Auto close", description = "Auto close wardrobe when you equip the armor", subcategory = "Wardrobe")
    public static boolean autoClose = true;
    @Switch(name = "Block unequip", description = "Prevents you unequip the armor", subcategory = "Wardrobe")
    public static boolean unEquip = false;
    @Switch(name = "Only block unequip in dungeon", description = "Only block unequip while in dungeon", subcategory = "Wardrobe")
    public static boolean blockInDungeon = true;
}
