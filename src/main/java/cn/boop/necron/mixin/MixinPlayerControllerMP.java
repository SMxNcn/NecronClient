package cn.boop.necron.mixin;

import cn.boop.necron.module.impl.item.GuiType;
import cn.boop.necron.module.impl.item.ItemProtector;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    private void onWindowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        if (isDropAction(slotId, mode)) {
            ItemStack itemToDrop = getItemFromSlot(windowId, slotId, mode, playerIn);

            if (itemToDrop != null && ItemProtector.shouldPreventDrop(itemToDrop)) {
                cir.cancel();
                ItemProtector.sendProtectMessage(itemToDrop, GuiType.DROP);
            }
        }
    }

    @Unique
    private boolean isDropAction(int slotId, int mode) {
        return slotId == -999 || mode == 4 || mode == 6;
    }

    @Unique
    private ItemStack getItemFromSlot(int windowId, int slotId, int mode, EntityPlayer player) {
        if (slotId == -999 && mode == 0) {
            return player.getHeldItem();
        }

        if ((mode == 4 || mode == 6) && player.openContainer != null) {
            if (slotId >= 0 && slotId < player.openContainer.inventorySlots.size()) {
                Slot slot = player.openContainer.getSlot(slotId);
                if (slot != null && slot.getHasStack()) {
                    return slot.getStack();
                }
            }
        }

        return null;
    }
}