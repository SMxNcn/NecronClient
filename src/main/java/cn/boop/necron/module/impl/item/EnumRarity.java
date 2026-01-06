package cn.boop.necron.module.impl.item;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EnumRarity {
    NONE("", "", new Color(255, 255, 255)),
    COMMON("§f", "COMMON", new Color(255, 255, 255)),
    UNCOMMON("§a", "UNCOMMON", new Color(77, 231, 77)),
    RARE("§9", "RARE", new Color(85, 85, 255)),
    EPIC("§5", "EPIC", new Color(151, 0, 151)),
    LEGENDARY("§6", "LEGENDARY", new Color(255, 170, 0)),
    MYTHIC("§d", "MYTHIC", new Color(255, 85, 255)),
    DIVINE("§b", "DIVINE", new Color(85, 255, 255)),
    SPECIAL("§c", "SPECIAL", new Color(255, 85, 85)),
    VERY_SPECIAL("§c", "VERY SPECIAL", new Color(170, 0, 0)),
    ULTIMATE("§4", "ULTIMATE", new Color(170, 0, 0));

    private final String colorCode;
    private final String displayName;
    private final Color color;

    EnumRarity(String colorCode, String displayName, Color color) {
        this.colorCode = colorCode;
        this.displayName = displayName;
        this.color = color;
    }

    public String getColorCode() {
        return colorCode;
    }

    public Color getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getGLRed() {
        return color.getRed() / 255f;
    }

    public float getGLGreen() {
        return color.getGreen() / 255f;
    }

    public float getGLBlue() {
        return color.getBlue() / 255f;
    }

    public static final Pattern RARITY_PATTERN = Pattern.compile(
            "(?:§[\\da-f]§l§ka§r )?" +
                    "(?:§[\\da-f]§l)?" +
                    "(?:SHINY )?" +
                    "(?<rarity>ULTIMATE|VERY\\sSPECIAL|SPECIAL|DIVINE|MYTHIC|LEGENDARY|EPIC|RARE|UNCOMMON|COMMON)"
    );

    public static EnumRarity parseRarity(String loreLine) {
        if (loreLine == null) return NONE;

        Matcher matcher = RARITY_PATTERN.matcher(loreLine);
        if (matcher.find()) {
            String matchedRarity = matcher.group("rarity").toUpperCase().replace(" ", "_");

            switch (matchedRarity) {
                case "ULTIMATE": return ULTIMATE;
                case "VERY_SPECIAL": return VERY_SPECIAL;
                case "SPECIAL": return SPECIAL;
                case "DIVINE": return DIVINE;
                case "MYTHIC": return MYTHIC;
                case "LEGENDARY": return LEGENDARY;
                case "EPIC": return EPIC;
                case "RARE": return RARE;
                case "UNCOMMON": return UNCOMMON;
                case "COMMON": return COMMON;
                default: return NONE;
            }
        }
        return NONE;
    }

    public static EnumRarity getRarityFromPetTier(String tier) {
        switch (tier.toUpperCase()) {
            case "COMMON": return COMMON;
            case "UNCOMMON": return UNCOMMON;
            case "RARE": return RARE;
            case "EPIC": return EPIC;
            case "LEGENDARY": return LEGENDARY;
            case "MYTHIC": return MYTHIC;
            default: return NONE;
        }
    }
}
