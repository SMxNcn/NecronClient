package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.LootProtector;
import cn.boop.necron.module.impl.hud.RngMeterHUD;
import cn.boop.necron.module.impl.rng.DungeonRngManager;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.*;

public class LootEventHandler {
    private boolean inRewardChest = false;
    private boolean inNormalChest = false;
    private boolean hasRareItems = false;
    private boolean messageSent = false;
    private boolean rngMsgSent = false;
    private boolean blockSent = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        String chestName;
        if (!(event.gui instanceof GuiChest) || !reroll || (!LocationUtils.inDungeon && !LocationUtils.getCurrentIslandName().equals("Dungeon Hub"))) {
            resetAllFlags();
            return;
        }
        GuiChest guiChest = (GuiChest) event.gui;
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        chestName = lowerChest.getDisplayName().getUnformattedText();

        inRewardChest = "Obsidian Chest".equals(chestName) || "Bedrock Chest".equals(chestName);
        inNormalChest = isNormalChest(chestName);

        if (!inRewardChest && !inNormalChest) {
            resetChestFlags();
        }
    }

    @SubscribeEvent
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if ((inNormalChest || inRewardChest) && hasRareItems && event.gui instanceof GuiChest) {
            if (!rerollProtect && !LocationUtils.inDungeon) return;
            if (isMouseButtonDown(0) || isMouseButtonDown(1)) {
                GuiChest guiChest = (GuiChest) event.gui;

                Slot slot = guiChest.getSlotUnderMouse();
                if (slot != null && slot.slotNumber == 50) {
                    ItemStack stack = slot.getStack();
                    if (isRerollButton(stack) && shouldDisableReroll() && blockSent) {
                        event.setCanceled(true);
                        Utils.modMessage("§cReroll button has been §lDISABLED§r§c!");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && (inRewardChest || inNormalChest)) {
            if (Necron.mc.currentScreen instanceof GuiChest) {
                checkAllItems((GuiChest) Necron.mc.currentScreen);
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        rngMsgSent = false;
    }

    private void checkForRareItems(GuiChest guiChest) {
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();

        if (LootProtector.hasRareLoot(lowerChest)) {
            if (!messageSent) {
                sendRareItemNames(lowerChest);
                messageSent = true;
            }
            hasRareItems = true;
        } else {
            hasRareItems = false;
        }
    }

    private void checkAllItems(GuiChest guiChest) {
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        String chestName = lowerChest.getDisplayName().getUnformattedText();
        if (!LocationUtils.inDungeon) return;

        String floor = LocationUtils.floor.name.replaceAll("[()]", "");

        for (int i = 0; i < lowerChest.getSizeInventory(); i++) {
            ItemStack stack = lowerChest.getStackInSlot(i);
            if (stack != null) {
                String itemName = Utils.removeFormatting(stack.getDisplayName());
                if (checkRngMeter(itemName, floor, chestName)) {
                    break;
                }
            }
        }

        if (inRewardChest) {
            checkForRareItems(guiChest);
        }
    }

    private void sendRareItemNames(IInventory inventory) {
        if (!LocationUtils.inDungeon) return;
        for (int i = 9; i <= 17 && i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) return;
            String itemName = Utils.removeFormatting(stack.getDisplayName());
            String chestName = inventory.getDisplayName().getUnformattedText();
            String floor = LocationUtils.floor.name.replaceAll("[()]", "");
            if (LootProtector.isRareItemByName(itemName)) {
                blockSent = true;
                System.out.println("Chest item: " + itemName);
                System.out.println("Called from floor: " + floor);
                if (!rngMsgSent) {
                    if (!checkRngMeter(itemName, floor, chestName))
                        Utils.modMessage("§dRng Item §7dropped! (" + stack.getDisplayName() + "§7)");
                    if (sendToParty && LocationUtils.inDungeon) {
                        if (memeRng) Utils.chatMessage("/pc NC » 我只是解锁了" + itemName + " 就被管家活活打断了双腿");
                        else Utils.modMessage("/pc NC » " + itemName + " in " + chestName + "!");
                    }
                    rngMsgSent = true;
                }
                break;
            }
        }
    }

    private boolean checkRngMeter(String droppedItemName, String floor, String chest) {
        RngMeterHUD.RngMeterData currentMeter = DungeonRngManager.INSTANCE.getMeterForFloor(floor);

        if (currentMeter != null && currentMeter.item != null && !currentMeter.item.isEmpty()) {
            String currentRngItem = Utils.removeFormatting(currentMeter.item);

            if (droppedItemName.contains(currentRngItem) && !rngMsgSent) {
                int score = currentMeter.score;
                double percentage = DungeonRngManager.INSTANCE.getCurrentFloorMeterPercentage();
                DungeonRngManager.INSTANCE.setScore(floor, 0);
                Utils.modMessage("§dRng Item §7reset! (§6" + Utils.addNumSeparator(score) + " §bScore, §6" + String.format("%.2f", percentage) + "§b%§7)");
                DungeonRngManager.INSTANCE.addScore(floor, DungeonRngEventHandler.getLastScore());
                DungeonRngEventHandler.setLastScore(0);
                if (sendToParty && LocationUtils.inDungeon) {
                    if (memeRng) Utils.chatMessage("/pc NC » 我只是解锁了" + droppedItemName + " 就被管家活活打断了双腿");
                    else Utils.modMessage("/pc NC » " + droppedItemName + " in " + chest + "!");
                }
                rngMsgSent = true;
                return true;
            }
        }
        return false;
    }

    private boolean isRerollButton(ItemStack stack) {
        if (stack == null) return false;
        String itemName = Utils.removeFormatting(stack.getDisplayName());
        return stack.getItem() == Items.feather && itemName.contains("Reroll Chest");
    }

    private boolean isNormalChest(String chestName) {
        return "Wood Chest".equals(chestName) ||
                "Gold Chest".equals(chestName) ||
                "Diamond Chest".equals(chestName) ||
                "Emerald Chest".equals(chestName);
    }

    private void resetAllFlags() {
        inRewardChest = false;
        inNormalChest = false;
        hasRareItems = false;
        messageSent = false;
        blockSent = false;
    }

    private void resetChestFlags() {
        hasRareItems = false;
        messageSent = false;
        blockSent = false;
    }

    private boolean isMouseButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }

    public boolean shouldDisableReroll() {
        return inRewardChest && hasRareItems && rerollProtect;
    }
}
