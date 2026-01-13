package cn.boop.necron.module.impl.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetInfo {
    private final String type;
    private final EnumRarity rarity;
    private final int level;
    private final double exp;
    private final String displayName;

    private static final Pattern PET_INFO_PATTERN = Pattern.compile("\\[Lvl\\s*(\\d+)]\\s*(§[0-9a-f])([^§]+)");

    public PetInfo(String type, EnumRarity rarity, int level, double exp, String displayName) {
        this.type = type;
        this.rarity = rarity;
        this.level = level;
        this.exp = exp;
        this.displayName = displayName;
    }

    public String getType() { return type; }
    public EnumRarity getRarity() { return rarity; }
    public int getLevel() { return level; }
    public double getExp() { return exp; }
    public String getDisplayName() { return displayName; }

    public String getItemId() {
        return getPetItemId(type, rarity);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Lvl %d - %.0f XP", displayName, rarity, level, exp);
    }

    public static PetInfo fromItemStack(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;

        NBTTagCompound tag = stack.getTagCompound();

        String type = null;
        EnumRarity rarity = EnumRarity.NONE;
        double exp = 0;

        if (tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound extraAttributes = tag.getCompoundTag("ExtraAttributes");
            if (extraAttributes.hasKey("petInfo")) {
                try {
                    String petInfoJson = extraAttributes.getString("petInfo");
                    JsonObject petInfoObj = new JsonParser().parse(petInfoJson).getAsJsonObject();

                    type = petInfoObj.has("type") ? petInfoObj.get("type").getAsString() : null;
                    String tier = petInfoObj.has("tier") ? petInfoObj.get("tier").getAsString() : null;
                    exp = petInfoObj.has("exp") ? petInfoObj.get("exp").getAsDouble() : 0;

                    if (tier != null) {
                        rarity = EnumRarity.getRarityFromPetTier(tier);
                    }
                } catch (Exception e) {
                    System.err.println("[PetInfo] Error parsing petInfo: " + e.getMessage());
                }
            }
        }

        if (type == null) return null;

        int level = 0;
        String displayName = null;

        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                String nameJson = display.getString("Name");

                level = getPetLevelFromName(nameJson);
                displayName = getPetDisplayNameFromName(nameJson);

                if (rarity == EnumRarity.NONE) rarity = getRarityFromNameColor(nameJson);
            }
        }

        return new PetInfo(type, rarity, level, exp, displayName);
    }

    public static int getPetLevelFromName(String nameJson) {
        if (nameJson == null) return 0;

        Matcher matcher = PET_INFO_PATTERN.matcher(nameJson);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static String getPetDisplayNameFromName(String nameJson) {
        if (nameJson == null) return null;

        Matcher matcher = PET_INFO_PATTERN.matcher(nameJson);
        if (matcher.find()) {
            return matcher.group(3).trim();
        }

        String cleaned = nameJson
                .replaceAll("§7\\[Lvl\\s*\\d+]", "")
                .replaceAll("§[0-9a-f]", "")
                .replaceAll("\\s+", " ")
                .trim();

        return cleaned.isEmpty() ? null : cleaned;
    }

    public static EnumRarity getRarityFromNameColor(String nameJson) {
        if (nameJson == null) return EnumRarity.NONE;

        Matcher matcher = PET_INFO_PATTERN.matcher(nameJson);
        if (matcher.find()) {
            String colorCode = matcher.group(2);
            return getRarityFromColorCode(colorCode);
        }

        return EnumRarity.NONE;
    }

    public static EnumRarity getRarityFromColorCode(String colorCode) {
        if (colorCode == null) return EnumRarity.NONE;

        switch (colorCode) {
            case "§f": return EnumRarity.COMMON;
            case "§a": return EnumRarity.UNCOMMON;
            case "§9": return EnumRarity.RARE;
            case "§5": return EnumRarity.EPIC;
            case "§6": return EnumRarity.LEGENDARY;
            case "§d": return EnumRarity.MYTHIC;
            default: return EnumRarity.NONE;
        }
    }

    public static String getPetItemId(String petName, EnumRarity rarity) {
        if (petName == null || petName.isEmpty() || rarity == null) {
            return null;
        }

        int rarityCode = getRarityCodeFromEnum(rarity);
        return String.format("PET_%s_%d", petName, rarityCode);
    }

    private static int getRarityCodeFromEnum(EnumRarity rarity) {
        if (rarity == null) return 0;

        switch (rarity) {
            case UNCOMMON: return 1;
            case RARE: return 2;
            case EPIC: return 3;
            case LEGENDARY: return 4;
            case MYTHIC: return 5;
            case COMMON:
            default: return 0;
        }
    }

    public static String getPetDisplayName(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                String nameJson = display.getString("Name");
                return getPetDisplayNameFromName(nameJson);
            }
        }
        return null;
    }

    public static int getPetLevel(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                String nameJson = display.getString("Name");
                return getPetLevelFromName(nameJson);
            }
        }
        return 0;
    }
}