package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.DungeonUtils;
import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.AutoLeapOptionsImpl.autoLeap;

public class AutoLeap {
    public static boolean inLeapGui = false;
    private static ContainerChest leapContainer;

    @SubscribeEvent()
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (Necron.mc.currentScreen instanceof GuiChest && LocationUtils.inSkyBlock && autoLeap) {
            GuiChest gui = (GuiChest) Necron.mc.currentScreen;
            leapContainer = (ContainerChest) gui.inventorySlots;
            IInventory inventory = leapContainer.getLowerChestInventory();
            String title = inventory.getDisplayName().getUnformattedText();
            if (!inLeapGui) inLeapGui = title.startsWith("Spirit Leap");
        } else {
            inLeapGui = false;
        }
    }

    public static boolean isLeapItem(ItemStack itemStack) {
        String skyBlockID = ItemUtils.getSkyBlockID(itemStack);
        return skyBlockID.contains("SPIRIT_LEAP") || skyBlockID.contains("INFINITE_SPIRIT_LEAP");
    }

    public static void leapToPlayer(String targetName) {
        if (!LocationUtils.inDungeon || !inLeapGui) return;

        for (int i = 9; i <= 17; i++) {
            if (i >= leapContainer.inventorySlots.size()) continue;

            Slot slot = leapContainer.getSlot(i);
            if (slot == null || !slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            String playerName = Utils.removeFormatting(stack.getDisplayName());
            if (targetName.equals(playerName)) {
                Utils.clickInventorySlot(i);
                return;
            }
        }
    }

    public static void leapToClass(DungeonUtils.DungeonClass playerClass) {
        if (!LocationUtils.inDungeon || !inLeapGui) return;

        for (int i = 9; i <= 17; i++) {
            if (i >= leapContainer.inventorySlots.size()) continue;

            Slot slot = leapContainer.getSlot(i);
            if (slot == null || !slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();
            String playerName = Utils.removeFormatting(stack.getDisplayName());

            DungeonUtils.DungeonPlayer dungeonPlayer = DungeonUtils.dungeonPlayers.get(playerName);
            if (dungeonPlayer != null && dungeonPlayer.getPlayerClass() == playerClass) {
                Utils.clickInventorySlot(i);
                return;
            }
        }
    }
}
