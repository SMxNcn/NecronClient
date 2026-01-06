package cn.boop.necron.module.impl.item;

import cn.boop.necron.Necron;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.util.List;

import static cn.boop.necron.config.impl.GUIOptionsImpl.rarityOpacity;

public class ItemOverlay {
    public static void renderRarityBackground(int slotX, int slotY, EnumRarity rarity) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Color color = rarity.getColor();
        int opacity = (int) (rarityOpacity / 100f * 255);
        GlStateManager.color(rarity.getGLRed(), rarity.getGLGreen(), rarity.getGLBlue(), 0.3f);

        Gui.drawRect(slotX, slotY, slotX + 16, slotY + 16, new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity).getRGB());

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popAttrib();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void renderHotbarRarityBackground(ScaledResolution sr) {
        EntityPlayerSP player = Necron.mc.thePlayer;
        if (player == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            EnumRarity rarity = ItemOverlay.getRarityFromStack(stack);
            if (rarity != EnumRarity.NONE) {
                int slotX = sr.getScaledWidth() / 2 - 90 + i * 20 + 2;
                int slotY = sr.getScaledHeight() - 16 - 3;
                ItemOverlay.renderRarityBackground(slotX, slotY, rarity);
            }
        }
    }

    public static EnumRarity getRarityFromStack(ItemStack stack) {
        if (stack == null) return EnumRarity.NONE;

        EnumRarity petRarity = getPetRarityFromNBT(stack);
        if (petRarity != EnumRarity.NONE) return petRarity;

        List<String> tooltip = stack.getTooltip(Necron.mc.thePlayer, false);
        for (String loreLine : tooltip) {
            EnumRarity rarity = EnumRarity.parseRarity(loreLine);
            if (rarity != EnumRarity.NONE) return rarity;
        }

        return EnumRarity.NONE;
    }

    private static EnumRarity getPetRarityFromNBT(ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag.hasKey("ExtraAttributes", 10)) {
                NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("petInfo")) {
                    String petInfoJson = extraAttributes.getString("petInfo");

                    try {
                        JsonObject petInfo = new JsonParser().parse(petInfoJson).getAsJsonObject();
                        if (petInfo.has("tier")) {
                            String tier = petInfo.get("tier").getAsString();
                            return EnumRarity.getRarityFromPetTier(tier);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return EnumRarity.NONE;
    }
}
