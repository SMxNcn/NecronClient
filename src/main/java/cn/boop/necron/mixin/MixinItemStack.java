package cn.boop.necron.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void replaceDyedWithColor(EntityPlayer playerIn, boolean advanced, CallbackInfoReturnable<List<String>> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (!(self.getItem() instanceof ItemArmor)) return;
        ItemArmor armor = (ItemArmor) self.getItem();
        if (armor.getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER) return;

        NBTTagCompound nbt = self.getTagCompound();
        if (nbt == null || !nbt.hasKey("display", 10)) return;

        NBTTagCompound display = nbt.getCompoundTag("display");
        if (!display.hasKey("color", 3)) return;

        int color = display.getInteger("color");
        String colorLine = "Color: #" + String.format("%06X", color & 0xFFFFFF);

        List<String> tooltip = cir.getReturnValue();
        String dyedText = EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.dyed");

        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).equals(dyedText)) {
                tooltip.set(i, colorLine);
                break;
            }
        }
    }
}
