package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import kotlin.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.*;

public class Utils {
    public static final Random random = new Random();

    public static void modMessage(String msg) {
        Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§bNecron §8»§r§7 " + msg));
    }

    public static void chatMessage(String cmd) {
        Necron.mc.thePlayer.sendChatMessage(cmd);
    }

    public static String removeFormatting(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    public static <T> T randomSelect(List<T> list) {
        int index = random.nextInt(list.size());
        return list.get(index);
    }

    public static String getSkyBlockID(ItemStack item) {
        if (item != null) {
            NBTTagCompound extraAttributes = item.getSubCompound("ExtraAttributes", false);
            if (extraAttributes != null && extraAttributes.hasKey("id")) {
                return extraAttributes.getString("id");
            }
        }
        return "";
    }

    public static <T> Pair<T, Double> weightedRandom(List<Pair<T, Double>> weightedList) {
        double total = weightedList.stream()
                .mapToDouble(Pair::getSecond)
                .sum();

        if (total == 0) return null;

        double randomPoint = random.nextDouble() * total;
        double cumulative = 0.0;

        for (Pair<T, Double> entry : weightedList) {
            cumulative += entry.getSecond();
            if (randomPoint <= cumulative) {
                return entry;
            }
        }
        return null;
    }

    public static int romanToInt(String roman) {
        if (roman.startsWith("0")) return 0;

        Map<Character, Integer> romanMap = new HashMap<>();
        romanMap.put('I', 1);
        romanMap.put('V', 5);
        romanMap.put('X', 10);
        romanMap.put('L', 50);
        romanMap.put('C', 100);

        int result = 0;

        for (int i = 0; i < roman.length(); i++) {
            int current = romanMap.get(roman.charAt(i));
            if (i < roman.length() - 1 && current < romanMap.get(roman.charAt(i + 1))) {
                result -= current;
            } else {
                result += current;
            }
        }

        return result;
    }

    public static String clearMcUsername(String username) {
        if (username == null || username.isEmpty()) return "";

        String cleanName = username.split(" ")[0];

        cleanName = cleanName.replaceAll("[^a-zA-Z0-9_]", "");

        if (cleanName.length() > 16) {
            cleanName = cleanName.substring(0, 16);
        }

        return cleanName;
    }

    public static String addNumSeparator(int number) {
        return String.format("%,d", number);
    }

    public static List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        if (Necron.mc.theWorld != null && Necron.mc.theWorld.playerEntities != null) {
            for (EntityPlayer entity : Necron.mc.theWorld.playerEntities) {
                if (entity instanceof EntityPlayer && !entity.getName().contains("§")) {
                    playerNames.add(clearMcUsername(entity.getName()));
                }
            }
        }
        return playerNames;
    }

    public static void clickInventorySlot(int slot) {
        try {
            if (Necron.mc.thePlayer == null || Necron.mc.thePlayer.openContainer == null) return;
            Container container = Necron.mc.thePlayer.openContainer;
            if (slot < 0 || slot >= container.inventorySlots.size()) return;
            Necron.mc.playerController.windowClick(container.windowId, slot, 0, 0, Necron.mc.thePlayer);
        } catch (Exception ignore) {
        }
    }

    public static void clickPlayerInventorySlot(int playerSlot) {
        try {
            if (Necron.mc.thePlayer == null || Necron.mc.thePlayer.openContainer == null) return;

            Container container = Necron.mc.thePlayer.openContainer;
            int containerSlots = container.inventorySlots.size();
            int actualSlot;

            if (playerSlot >= 0 && playerSlot < 9) {
                actualSlot = containerSlots - 9 + playerSlot;
            } else if (playerSlot >= 9 && playerSlot < 36) {
                int containerBaseSlots = containerSlots - 36;
                if (containerBaseSlots < 0) return;
                actualSlot = containerBaseSlots + (playerSlot - 9);
            } else {
                return;
            }

            if (actualSlot < 0 || actualSlot >= containerSlots) return;
            Necron.mc.playerController.windowClick(container.windowId, actualSlot, 0, 0, Necron.mc.thePlayer);
        } catch (Exception ignore) {
        }
    }

    public static boolean isPlayerInArea(BlockPos pos1, BlockPos pos2, BlockPos playerPos) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return playerPos.getX() >= minX && playerPos.getX() <= maxX &&
                playerPos.getY() >= minY && playerPos.getY() <= maxY &&
                playerPos.getZ() >= minZ && playerPos.getZ() <= maxZ;
    }
}
