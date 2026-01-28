package cn.boop.necron.module.impl;

import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

import static cn.boop.necron.config.impl.WardrobeOptionsImpl.*;

public class AutoWardrobe {
    private boolean isInWardrobe = false;
    private final boolean[] keyStates = new boolean[11];
    private final OneKeyBind[] equipmentKeys = {wd0, wd1, wd2, wd3, wd4, wd5, wd6, wd7, wd8};
    private long lastPressTime = 0;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChest && LocationUtils.inSkyBlock && wardrobe) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) gui.inventorySlots;
            IInventory inventory = container.getLowerChestInventory();
            String title = inventory.getDisplayName().getUnformattedText();

            isInWardrobe = title.startsWith("Wardrobe (");
        } else if (event.gui == null) {
            isInWardrobe = false;
            resetKeyStates();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Necron.mc.thePlayer == null || Necron.mc.theWorld == null || Necron.mc.currentScreen == null || !wardrobe || !isInWardrobe) return;
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < equipmentKeys.length; i++) {
            if (checkKeyPress(equipmentKeys[i], i, currentTime)) handleEquipmentKey(i);
        }

        if (checkKeyPress(prevPage, 9, currentTime)) handlePageNavigation(true);
        if (checkKeyPress(nextPage, 10, currentTime)) handlePageNavigation(false);
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isInWardrobe || !(event.gui instanceof GuiChest) || !wardrobe) return;

        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey();

            if ((key >= Keyboard.KEY_1 && key <= Keyboard.KEY_9) || key == Keyboard.KEY_MINUS || key == Keyboard.KEY_EQUALS) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isInWardrobe || !(event.gui instanceof GuiChest) || !wardrobe || Necron.mc.currentScreen == null) return;

        if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
            GuiChest gui = (GuiChest) event.gui;
            Slot hoveredSlot = gui.getSlotUnderMouse();

            if (hoveredSlot != null && hoveredSlot.slotNumber >= 36 && hoveredSlot.slotNumber <= 44) {
                if (shouldCancelEquipmentAction(hoveredSlot.slotNumber)) event.setCanceled(true);
                else if (autoClose) closeWardrobe();
            }
        }

    }

    private boolean checkKeyPress(OneKeyBind keyBind, int keyIndex, long currentTime) {
        if (keyBind == null) return false;

        boolean keyPressed = keyBind.isActive();
        boolean wasPressed = keyStates[keyIndex];

        if (keyPressed && !wasPressed) {
            keyStates[keyIndex] = true;
            if (currentTime - lastPressTime > 80) {
                lastPressTime = currentTime;
                return true;
            }
        } else if (!keyPressed) {
            keyStates[keyIndex] = false;
        }

        return false;
    }

    private void resetKeyStates() {
        Arrays.fill(keyStates, false);
    }

    private void handleEquipmentKey(int slotIndex) {
        if (!isInWardrobe || !(Necron.mc.currentScreen instanceof GuiChest)) return;

        int guiSlot = 36 + slotIndex;
        if (shouldCancelEquipmentAction(guiSlot)) return;

        Utils.clickInventorySlot(guiSlot);
        if (autoClose) closeWardrobe();
    }

    private boolean shouldCancelEquipmentAction(int guiSlot) {
        boolean isEquippedSlot = false;
        if (Necron.mc.currentScreen instanceof GuiChest) {
            GuiChest gui = (GuiChest) Necron.mc.currentScreen;
            if (guiSlot >= 36 && guiSlot <= 44) {
                Slot slot = gui.inventorySlots.getSlot(guiSlot);
                if (slot != null && slot.getHasStack()) {
                    ItemStack stack = slot.getStack();
                    if (stack != null && stack.getItem() == Items.dye) {
                        EnumDyeColor dyeColor = EnumDyeColor.byDyeDamage(stack.getMetadata());
                        isEquippedSlot = dyeColor == EnumDyeColor.LIME;
                    }
                }
            }
        }

        if (!unEquip) return false;
        return blockInDungeon ? LocationUtils.inDungeon && isEquippedSlot : isEquippedSlot;
    }

    private void handlePageNavigation(boolean isPreviousPage) {
        if (!isInWardrobe || !(Necron.mc.currentScreen instanceof GuiChest)) return;

        int pageSlot = isPreviousPage ? 45 : 53;

        GuiChest gui = (GuiChest) Necron.mc.currentScreen;
        if (pageSlot < gui.inventorySlots.inventorySlots.size()) {
            Slot guiSlot = gui.inventorySlots.getSlot(pageSlot);

            if (guiSlot != null && guiSlot.getHasStack()) {
                ItemStack stack = guiSlot.getStack();
                if (stack.getItem() == Items.arrow) {
                    Utils.clickInventorySlot(pageSlot);
                }
            }
        }
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
