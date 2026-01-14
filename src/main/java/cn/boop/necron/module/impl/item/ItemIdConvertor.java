package cn.boop.necron.module.impl.item;

import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

public class ItemIdConvertor {
    static {
        ItemIdMap.initializeConversionMap();
    }

    public static String convertDisplayNameToApiId(String displayName) {
        String cleanName = Utils.removeFormatting(displayName).replaceAll(" x\\d+$", "");

        if (ItemIdMap.API_ID_MAP.containsKey(cleanName)) {
            return ItemIdMap.API_ID_MAP.get(cleanName);
        }

        if (cleanName.startsWith("Enchanted Book")) {
            return convertEnchantedBookToApiId(cleanName);
        }

        if (cleanName.endsWith("Essence")) {
            String essenceType = cleanName.replace(" ", "_").toUpperCase();
            if (essenceType.endsWith("_ESSENCE")) {
                String namePart = essenceType.substring(0, essenceType.length() - 8);
                return "ESSENCE_" + namePart;
            }
            return essenceType;
        }

        if (cleanName.endsWith("Shard")) {
            String shardType = cleanName.replace(" ", "_").toUpperCase();
            if (shardType.endsWith("_SHARD")) {
                String namePart = shardType.substring(0, shardType.length() - 6);
                return "SHARD_" + namePart;
            }
            return "SHARD_" + shardType;
        }

        return cleanName.toUpperCase().replace(" ", "_").replace("'S", "").replace("-", "_");
    }

    private static String convertEnchantedBookToApiId(String displayName) {
        String cleanName = Utils.removeFormatting(displayName);
        if (cleanName.startsWith("Enchanted Book (")) {
            int startIndex = "Enchanted Book (".length();
            int endIndex = cleanName.length() - 1;

            if (endIndex > startIndex) {
                String enchantmentPart = cleanName.substring(startIndex, endIndex);

                String[] parts = enchantmentPart.trim().split(" ");
                if (parts.length >= 2) {
                    String level = parts[parts.length - 1];
                    String enchantmentName = String.join("_", Arrays.copyOf(parts, parts.length - 1));

                    int levelInt = Utils.romanToInt(level);
                    if (levelInt == 0 && Character.isDigit(level.charAt(0))) {
                        levelInt = Integer.parseInt(level);
                    }

                    enchantmentName = enchantmentName.toUpperCase().replace(" ", "_").replace("-", "_");
                    return "ENCHANTMENT_" + enchantmentName + "_" + levelInt;
                } else if (parts.length == 1) {
                    String enchantmentName = parts[0].toUpperCase().replace(" ", "_").replace("-", "_");
                    return "ENCHANTMENT_" + enchantmentName + "_1";
                }
            }
        }

        return "ENCHANTED_BOOK";
    }

    public static String getApiIdFromItemStack(ItemStack itemStack) {
        if (itemStack == null) return null;

        if (isEnchantedBook(itemStack)) {
            String enchantmentApiId = getEnchantmentApiIdFromNBT(itemStack);
            if (enchantmentApiId != null) {
                return enchantmentApiId;
            }
        }

        String skyBlockId = ItemUtils.getSkyBlockID(itemStack);
        if (!skyBlockId.isEmpty()) {
            return skyBlockId;
        }

        PetInfo petInfo = PetInfo.fromItemStack(itemStack);
        if (petInfo != null) {
            return petInfo.getItemId();
        }

        String displayName = itemStack.getDisplayName();
        return convertDisplayNameToApiId(displayName);
    }

    private static boolean isEnchantedBook(ItemStack itemStack) {
        if (itemStack == null) return false;
        String displayName = itemStack.getDisplayName();
        return Utils.removeFormatting(displayName).startsWith("Enchanted Book");
    }

    private static String getEnchantmentApiIdFromNBT(ItemStack itemStack) {
        NBTTagCompound extraAttributes = itemStack.getSubCompound("ExtraAttributes", false);
        if (extraAttributes != null && extraAttributes.hasKey("enchantments")) {
            NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
            for (String enchantmentName : enchantments.getKeySet()) {
                int level = enchantments.getInteger(enchantmentName);

                String formattedEnchantmentName;

                if (enchantmentName.toLowerCase().startsWith("ultimate_")) {
                    formattedEnchantmentName = enchantmentName.toUpperCase();
                } else {
                    formattedEnchantmentName = enchantmentName.toUpperCase().replace(" ", "_").replace("-", "_");
                }

                return "ENCHANTMENT_" + formattedEnchantmentName + "_" + level;
            }
        }
        return null;
    }
}
