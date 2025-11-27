package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.*;

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
}
