package cn.boop.necron.mixin;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.hud.ChestProfitHUD;
import cn.boop.necron.module.impl.item.*;
import cn.boop.necron.utils.LocationUtils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.boop.necron.config.impl.GUIOptionsImpl.chestProfit;
import static cn.boop.necron.config.impl.GUIOptionsImpl.displayRarity;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Unique private ItemStack draggedItem = null;
    @Unique private ItemStack pendingDropItem = null;
    @Unique private ItemStack lastHoveredItem = null;
    @Unique private Slot lastHoveredSlot = null;
    @Unique private boolean isDragging = false;
    @Unique private boolean isClickingOutside = false;
    @Unique private long lastPickupTime = 0;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        RarityRenderer.beginFrame();
        GuiContainer gui = (GuiContainer) (Object) this;
        lastHoveredSlot = gui.getSlotUnderMouse();

        if (lastHoveredSlot != null && lastHoveredSlot.getHasStack()) {
            lastHoveredItem = lastHoveredSlot.getStack();
        } else if (gui.mc.currentScreen != null && mouseX >= 0 && mouseX <= gui.mc.currentScreen.width && mouseY >= 0 && mouseY <= gui.mc.currentScreen.height) {
            lastHoveredItem = null;
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V", shift = At.Shift.AFTER))
    private void onDrawScreenBeforeTooltipCondition(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (chestProfit && Necron.mc.currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) Necron.mc.currentScreen;

            if (ChestProfitHUD.isDungeonRewardOverview(guiChest)) {
                ChestProfitHUD.onRenderDungeonRewardOverview(guiChest);
            } else {
                ChestProfitHUD.onRenderChest(guiChest);
            }
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void onDrawScreenTail(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        RarityRenderer.endFrame();
    }

    @Inject(method = "handleMouseClick", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        GuiContainer gui = (GuiContainer) (Object) this;

        if (slotIn != null && slotIn.getHasStack() && slotId >= 0 && Necron.mc.currentScreen instanceof GuiChest) {
            ItemStack itemToCheck = slotIn.getStack();
            GuiChest chestGui = (GuiChest) gui;

            if (ItemProtector.shouldPreventGuiClick(chestGui, itemToCheck)) {
                ci.cancel();
                ItemProtector.sendProtectMessage(itemToCheck, ItemProtector.getGuiType(chestGui));
                return;
            }
        }
        
        if (slotId == -999 && clickType == 0) {
            isClickingOutside = true;

            ItemStack itemToCheck = getItemToCheck(gui);

            if (itemToCheck != null) {
                boolean isProtected = ItemProtector.isItemProtected(itemToCheck);

                if (isProtected) {
                    ci.cancel();
                    ItemProtector.sendProtectMessage(itemToCheck, GuiType.DROP);

                    isClickingOutside = false;
                    return;
                }

                pendingDropItem = null;
            }

            isClickingOutside = false;
        } else if (slotId >= 0 && clickType == 0 && clickedButton == 0) {
            if (slotIn != null && slotIn.getHasStack()) {
                pendingDropItem = slotIn.getStack().copy();
            }
        } else if (clickType == 4) {
            if (slotIn != null && slotIn.getHasStack()) {
                ItemStack itemToCheck = slotIn.getStack();
                boolean isProtected = ItemProtector.isItemProtected(itemToCheck);

                if (isProtected) {
                    ci.cancel();
                    ItemProtector.sendProtectMessage(itemToCheck, GuiType.DROP);
                    return;
                }
            }
        } else if (clickType == 6) {
            ItemStack itemToCheck = null;

            if (lastHoveredItem != null) itemToCheck = lastHoveredItem;
            else if (slotIn != null && slotIn.getHasStack()) itemToCheck = slotIn.getStack();

            if (itemToCheck != null) {
                boolean isProtected = ItemProtector.isItemProtected(itemToCheck);

                if (isProtected) {
                    ci.cancel();
                    ItemProtector.sendProtectMessage(itemToCheck, GuiType.DROP);
                    return;
                }
            }
        } else if (slotId >= 0 && clickType == 0 && clickedButton == 1) {
            if (slotIn != null && slotIn.getHasStack()) {
                ItemStack itemToCheck = slotIn.getStack();
                boolean isProtected = ItemProtector.isItemProtected(itemToCheck);

                if (isProtected) {
                    ci.cancel();
                    ItemProtector.sendProtectMessage(itemToCheck, GuiType.DROP);
                    return;
                }
            }
        } else if (slotId == -999 && clickType == 1) {
            if (draggedItem != null) {
                boolean isProtected = ItemProtector.isItemProtected(draggedItem);

                if (isProtected) {
                    ci.cancel();
                    ItemProtector.sendProtectMessage(draggedItem, GuiType.DROP);
                    draggedItem = null;
                    isDragging = false;
                    return;
                }
            }
        }

        if (slotIn != null && slotIn.getHasStack() &&
                clickType == 0 && (clickedButton == 0 || clickedButton == 1)) {
            draggedItem = slotIn.getStack().copy();
        }
    }

    @Inject(method = "mouseClickMove", at = @At("HEAD"), cancellable = true)
    private void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick, CallbackInfo ci) {
        if (clickedMouseButton == 0 || clickedMouseButton == 1) {
            isDragging = true;
            long currentTime = System.currentTimeMillis();

            if (draggedItem != null && ItemProtector.isItemProtected(draggedItem) && (currentTime - lastPickupTime > 100)) {
                ci.cancel();
                ItemProtector.sendProtectMessage(draggedItem, GuiType.DROP);
                lastPickupTime = currentTime;
                draggedItem = null;
                isDragging = false;
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onMouseReleased(int mouseX, int mouseY, int state, CallbackInfo ci) {
        isDragging = false;

        if (isClickingOutside) pendingDropItem = null;
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(Slot slotIn, CallbackInfo ci) {
        if (slotIn.getHasStack() && LocationUtils.inSkyBlock && displayRarity) {
            EnumRarity rarity = ItemOverlay.getRarityFromStack(slotIn.getStack());
            if (rarity != EnumRarity.NONE) {
                ItemOverlay.submitRarityBackground(slotIn.xDisplayPosition, slotIn.yDisplayPosition, rarity);
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V", ordinal = 0/*, shift = At.Shift.BEFORE*/))
    private void onBeforeRenderItem(Slot slotIn, CallbackInfo ci) {
        if (slotIn.getHasStack() && LocationUtils.inSkyBlock && displayRarity) {
            RarityRenderer.flushBatch();
        }
    }

    @Unique
    private ItemStack getItemToCheck(GuiContainer gui) {
        ItemStack itemToCheck = pendingDropItem;

        if (itemToCheck == null && lastHoveredItem != null) {
            itemToCheck = lastHoveredItem;
        }
        else if (itemToCheck == null && draggedItem != null) {
            itemToCheck = draggedItem;
        }
        else if (itemToCheck == null && gui.mc.thePlayer != null) {
            ItemStack heldItem = gui.mc.thePlayer.getHeldItem();
            if (heldItem != null) {
                itemToCheck = heldItem;
            }
        }
        return itemToCheck;
    }
}