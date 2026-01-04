package cn.boop.necron.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @ModifyVariable(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getCompoundTag(Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;", ordinal = 0), ordinal = 0, argsOnly = true)
    private boolean forceShowColor(boolean advanced, EntityPlayer playerIn, boolean originalAdvanced) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();
            if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER) {
                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null && nbt.hasKey("display", 10) && nbt.getCompoundTag("display").hasKey("color", 3)) {
                    return true;
                }
            }
        }

        return advanced;
    }
}
