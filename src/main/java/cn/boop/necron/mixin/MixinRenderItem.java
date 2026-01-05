package cn.boop.necron.mixin;

import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.boop.necron.config.impl.GUIOptionsImpl.displayUpgrade;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"), cancellable = true)
    private void renderUpgradeLevelAsOverlay(FontRenderer fontRenderer, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null || Utils.getSkyBlockID(stack).isEmpty() || !displayUpgrade) return;

        try {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
                NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
                if (extra.hasKey("upgrade_level", 3)) {
                    int level = extra.getInteger("upgrade_level");
                    if (level >= 1) {
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableBlend();

                        String count = String.valueOf(level);
                        fontRenderer.drawStringWithShadow(count, x + 19 - 2 - fontRenderer.getStringWidth(count), y + 6 +3, 16777215);

                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        GlStateManager.enableBlend();
                        ci.cancel();
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
