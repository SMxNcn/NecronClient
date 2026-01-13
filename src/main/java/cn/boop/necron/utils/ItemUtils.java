package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ItemUtils {
    public static boolean isHoldingSword() {
        ItemStack itemStack = Necron.mc.thePlayer.getHeldItem();
        if (itemStack == null) {
            return false;
        }
        return itemStack.getItem() instanceof ItemSword;
    }

    public static boolean isHoldingTool() {
        ItemStack itemStack = Necron.mc.thePlayer.getHeldItem();
        if (itemStack == null) {
            return false;
        }
        return itemStack.getItem() instanceof ItemTool;
    }

    public static boolean isHoldingBlock() {
        return isBlock(Necron.mc.thePlayer.getHeldItem());
    }

    public static boolean isBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.stackSize < 1) {
            return false;
        }
        Item item = itemStack.getItem();
        return item instanceof ItemBlock && !isContainerBlock((ItemBlock) item);
    }

    public static boolean isContainerBlock(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        return block instanceof BlockChest ||
                block instanceof BlockFurnace ||
                block instanceof BlockDispenser;
    }

    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack == null) return null;
        return itemStack.getTooltip(Necron.mc.thePlayer, true);
    }

    public static String getItemLoreLine(ItemStack itemStack, int index) {
        if (itemStack == null) return null;

        List<String> lore = getItemLore(itemStack);
        if (index < 0 || index >= lore.size()) return null;
        return lore.get(index);
    }

    public static String getSkyBlockID(ItemStack item) {
        if (item != null) {
            NBTTagCompound extraAttributes = item.getSubCompound("ExtraAttributes", false);
            if (extraAttributes != null && extraAttributes.hasKey("id")) {
                return extraAttributes.getString("id");
            }
        }
        return "";
    }

    public static String getItemUUID(ItemStack item) {
        if (item != null) {
            NBTTagCompound extraAttributes = item.getSubCompound("ExtraAttributes", false);
            if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
                return extraAttributes.getString("uuid");
            }
        }
        return "";
    }

    public static int getItemUpgradeLevel(ItemStack item) {
        if (item != null) {
            NBTTagCompound extraAttributes = item.getSubCompound("ExtraAttributes", false);
            if (extraAttributes != null && extraAttributes.hasKey("upgrade_level")) {
                return extraAttributes.getInteger("upgrade_level");
            }
        }
        return 0;
    }
}
