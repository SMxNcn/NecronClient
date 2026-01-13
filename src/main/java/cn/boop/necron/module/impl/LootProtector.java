package cn.boop.necron.module.impl;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LootProtector {
    public enum PriceType {
        AUCTION,
        BAZAAR
    }

    private static final Map<String, PriceType> RARE_ITEM_MAP = new HashMap<>();
    private static int rareItemSlot = -1;

    static {
        RARE_ITEM_MAP.put("Shiny Necron's Handle", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Necron's Handle", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Implosion", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("Wither Shield", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("Shadow Warp", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Dark Claymore", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Giant's Sword", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Shadow Fury", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Necron Dye", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Livid Dye", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Master Skull - Tier 5", PriceType.AUCTION);
        RARE_ITEM_MAP.put("Fifth Master Star", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("Fourth Master Star", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("Third Master Star", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("Second Master Star", PriceType.BAZAAR);
        RARE_ITEM_MAP.put("First Master Star", PriceType.BAZAAR);
    }

    public static boolean hasRareLoot(IInventory inventory) {
        for (int i = 9; i <= 26 && i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                rareItemSlot = i;
                return isRareItemByName(stack.getDisplayName());
            }
        }
        return false;
    }

    public static boolean isRareItemByName(String itemName) {
        return RARE_ITEM_MAP.containsKey(itemName);
    }

    public static PriceType getPriceType(String itemName) {
        return RARE_ITEM_MAP.getOrDefault(itemName, null);
    }

    public static int getRareItemSlot() {
        return rareItemSlot;
    }
}
