package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.HUD.RNGMeterHUD;
import cn.boop.necron.module.impl.LootProtector;
import cn.boop.necron.module.impl.ctjs.RngMeterManager;
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

import static cn.boop.necron.config.impl.RerollProtectOptionsImpl.*;

public class LootEventHandler {
    private boolean inRewardChest = false;
    private boolean hasRareItems = false;
    private boolean messageSent = false;
    private boolean rngMsgSent = false;
    private boolean blockSent = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        String chestName;
        if (event.gui instanceof GuiChest && reroll && (LocationUtils.inDungeon || LocationUtils.getCurrentIslandName().equals("Dungeon Hub"))) {
            GuiChest guiChest = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) guiChest.inventorySlots;
            IInventory lowerChest = container.getLowerChestInventory();
            chestName = lowerChest.getDisplayName().getUnformattedText();

            inRewardChest = "Obsidian Chest".equals(chestName) || "Bedrock Chest".equals(chestName);

            if (!inRewardChest) {
                hasRareItems = false;
                messageSent = false;
                blockSent = false;
            }
        } else {
            hasRareItems = false;
            messageSent = false;
            blockSent = false;
            inRewardChest = false;
        }
    }

    @SubscribeEvent
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (inRewardChest && hasRareItems && rerollProtect && event.gui instanceof GuiChest && LocationUtils.inDungeon) {
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
        if (event.phase == TickEvent.Phase.START && inRewardChest) {
            if (Necron.mc.currentScreen instanceof GuiChest) {
                checkForRareItems((GuiChest) Necron.mc.currentScreen);
            }
        }
    }

    @SubscribeEvent
    public void onGuiClose(WorldEvent.Load event) {
        rngMsgSent = false;
    }

    private void checkForRareItems(GuiChest guiChest) {
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        int size = lowerChest.getSizeInventory();
        ItemStack[] items = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            items[i] = lowerChest.getStackInSlot(i);
        }

        if (LootProtector.hasRareLoot(items)) {
            if (!messageSent) {
                sendRareItemNames(items);
                messageSent = true;
            }
            hasRareItems = true;
        } else {
            hasRareItems = false;
        }
    }

    private void sendRareItemNames(ItemStack[] items) {
        for (int i = 9; i <= 17 && i < items.length; i++) {
            if (items[i] != null) {
                String itemName = Utils.removeFormatting(items[i].getDisplayName());
                String floor = LocationUtils.floor.name.replaceAll("[()]", "");
                if (LootProtector.isRareItemByName(itemName)) {
                    blockSent = true;
                    System.out.println("Chest item: " + itemName);
                    System.out.println("Called from floor: " + floor);
                    if (!rngMsgSent) {
                        if (!checkRngMeter(itemName, floor))
                            Utils.modMessage("§dRng Item §7dropped! (" + items[i].getDisplayName() + "§7)");
                        if (sendToParty && LocationUtils.inDungeon) {
                            Utils.chatMessage("/pc NC » 我只是解锁了" + itemName + " 就被管家活活打断了双腿");
                        }
                        rngMsgSent = true;
                    }
                    break;
                }
            }
        }
    }

    private boolean isRerollButton(ItemStack stack) {
        if (stack == null) return false;
        String itemName = Utils.removeFormatting(stack.getDisplayName());
        return stack.getItem() == Items.feather && itemName.contains("Reroll Chest");
    }

    private boolean checkRngMeter(String droppedItemName, String floor) {
        System.out.println("droppedItemName: " + droppedItemName);
        RNGMeterHUD.RngMeterData currentMeter = RngMeterManager.INSTANCE.getMeterForFloor(floor);

        if (currentMeter != null && currentMeter.item != null && !currentMeter.item.isEmpty()) {
            String currentRngItem = Utils.removeFormatting(currentMeter.item);
            System.out.println("Current Rng Item: " + currentRngItem);
            System.out.println("Contains check: " + droppedItemName.contains(currentRngItem));

            if (droppedItemName.contains(currentRngItem) && !rngMsgSent) {
                int score = currentMeter.score;
                double percentage = RngMeterManager.INSTANCE.getCurrentFloorMeterPercentage();
                RngMeterManager.INSTANCE.setScore(floor, 0);
                Utils.modMessage("§dRng Item §7reset! (§6" + score + " §bScore, §6" + String.format("%.2f", percentage) + "§b%§7)");
                rngMsgSent = true;
                return true;
            }
        }
        return false;
    }

    private boolean isMouseButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }

    public boolean shouldDisableReroll() {
        return inRewardChest && hasRareItems && rerollProtect;
    }
}
