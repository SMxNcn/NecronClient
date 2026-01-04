package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cn.boop.necron.config.ModConfig;
import org.lwjgl.input.Keyboard;

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

    @KeyBind(name = "Wardrobe 1", category = "Keybinds")
    public static OneKeyBind wd0 = new OneKeyBind(Keyboard.KEY_1);
    @KeyBind(name = "Wardrobe 2", category = "Keybinds")
    public static OneKeyBind wd1 = new OneKeyBind(Keyboard.KEY_2);
    @KeyBind(name = "Wardrobe 3", category = "Keybinds")
    public static OneKeyBind wd2 = new OneKeyBind(Keyboard.KEY_3);
    @KeyBind(name = "Wardrobe 4", category = "Keybinds")
    public static OneKeyBind wd3 = new OneKeyBind(Keyboard.KEY_4);
    @KeyBind(name = "Wardrobe 5", category = "Keybinds")
    public static OneKeyBind wd4 = new OneKeyBind(Keyboard.KEY_5);
    @KeyBind(name = "Wardrobe 6", category = "Keybinds")
    public static OneKeyBind wd5 = new OneKeyBind(Keyboard.KEY_6);
    @KeyBind(name = "Wardrobe 7", category = "Keybinds")
    public static OneKeyBind wd6 = new OneKeyBind(Keyboard.KEY_7);
    @KeyBind(name = "Wardrobe 8", category = "Keybinds")
    public static OneKeyBind wd7 = new OneKeyBind(Keyboard.KEY_8);
    @KeyBind(name = "Wardrobe 9", category = "Keybinds")
    public static OneKeyBind wd8 = new OneKeyBind(Keyboard.KEY_9);
    @KeyBind(name = "Previous Page", category = "Keybinds")
    public static OneKeyBind prevPage = new OneKeyBind(Keyboard.KEY_MINUS);
    @KeyBind(name = "Next Page", category = "Keybinds")
    public static OneKeyBind nextPage = new OneKeyBind(Keyboard.KEY_EQUALS);
}
