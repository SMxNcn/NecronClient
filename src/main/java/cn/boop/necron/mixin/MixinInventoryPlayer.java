package cn.boop.necron.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.boop.necron.config.impl.ScrollingOptionsImpl.*;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
    @Inject(method = "changeCurrentItem", at = @At("HEAD"))
    private void onChangeCurrentItem(int direction, CallbackInfo ci) {
        if (hotbarSmoothness == 0 || !smoothHotbarSc || Minecraft.getMinecraft().thePlayer == null) return;

        InventoryPlayer inv = (InventoryPlayer) (Object) this;
        int currentSlot = inv.currentItem;

        int newSlot = currentSlot + direction;
        if (newSlot < 0) newSlot = 8;
        if (newSlot > 8) newSlot = 0;

        if (currentSlot == 8 && newSlot == 0) {
            hotbarRollover -= 1;
        } else if (currentSlot == 0 && newSlot == 8) {
            hotbarRollover += 1;
        }
    }
}
