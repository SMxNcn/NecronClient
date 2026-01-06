package cn.boop.necron.mixin;

import cn.boop.necron.module.impl.item.GuiType;
import cn.boop.necron.module.impl.item.ItemProtector;
import cn.boop.necron.utils.LocationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    private void onDropOneItem(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        ItemStack heldItem = player.getHeldItem();

        if (heldItem != null && ItemProtector.shouldPreventDrop(heldItem) && !LocationUtils.inDungeon) {
            cir.cancel();
            ItemProtector.sendProtectMessage(heldItem, GuiType.DROP);
        }
    }
}