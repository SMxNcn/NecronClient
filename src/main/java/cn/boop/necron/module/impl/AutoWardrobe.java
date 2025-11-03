package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static cn.boop.necron.config.impl.WardrobeOptionsImpl.*;

public class AutoWardrobe {
    private boolean isInWardrobe = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChest && LocationUtils.inSkyBlock && wardrobe) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) gui.inventorySlots;
            IInventory inventory = container.getLowerChestInventory();
            String title = inventory.getDisplayName().getUnformattedText();

            isInWardrobe = title.startsWith("Wardrobe (");
        }
    }

    @SubscribeEvent
    public void onGuiClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (isInWardrobe && Mouse.isButtonDown(0) && event.gui instanceof GuiChest) {
            GuiChest gui = (GuiChest) event.gui;
            Slot slot = gui.getSlotUnderMouse();

            if (slot != null && slot.slotNumber >= 36 && slot.slotNumber <= 44) {
                ItemStack stack = slot.getStack();
                if (stack != null && stack.getItem() instanceof ItemDye) {
                    EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(stack.getMetadata());

                    if (dyeColor == EnumDyeColor.LIME) {
                        if (unEquip || (LocationUtils.inDungeon && blockInDungeon)) {
                            event.setCanceled(true);
                        }
                    } else {
                        closeWardrobe();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (isInWardrobe && event.gui instanceof GuiChest) {
            if (Keyboard.getEventKeyState()) {
                int key = Keyboard.getEventKey();

                if (shouldCancelKeyPress(key)) {
                    event.setCanceled(true);
                } else if (key >= 2 && key <= 10) {
                    closeWardrobe();
                }
            }
        }
    }

    private boolean shouldCancelKeyPress(int key) {
        if (!unEquip) return false;
        if (blockInDungeon && !LocationUtils.inDungeon) return false;

        int slotNumber = 35 + (key - 1);

        if (Necron.mc.currentScreen instanceof GuiChest) {
            GuiChest gui = (GuiChest) Necron.mc.currentScreen;
            if (gui.inventorySlots instanceof ContainerChest) {
                if (slotNumber >= 0 && slotNumber < gui.inventorySlots.inventorySlots.size()) {
                    Slot slot = gui.inventorySlots.getSlot(slotNumber);
                    if (slot != null && slot.getHasStack()) {
                        ItemStack stack = slot.getStack();
                        if (stack != null && stack.getItem() instanceof ItemDye) {
                            EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(stack.getMetadata());
                            return dyeColor == EnumDyeColor.LIME;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void closeWardrobe() {
        new Thread(() -> {
            try {
                int delay = (int) (Math.random() * 200) + 300;
                Thread.sleep(delay);
                if (Necron.mc.thePlayer != null && Necron.mc.getNetHandler() != null && autoClose) {
                    Necron.mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow());
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
