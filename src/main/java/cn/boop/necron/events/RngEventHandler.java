package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.ctjs.RngMeterManager;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.daemonLevel;
import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.hasDaemon;

public class RngEventHandler {
    private static final Pattern SCORE_PATTERN = Pattern.compile("Team Score:\\s*(\\d+)\\s*\\((S\\+?)\\)$");
    private static final Pattern RESET_PATTERN = Pattern.compile("You reset your selected drop for your Catacombs \\((\\w{1,2})\\) RNG Meter!");
    private static final Pattern SET_PATTERN = Pattern.compile("§r§aYou set your §r§dCatacombs \\((\\w{1,2})\\) RNG Meter §r§ato drop §r(.+)§r§a!§r");

    private static final Pattern FLOOR_PATTERN = Pattern.compile("^Catacombs \\((\\w{1,2})\\)$");
    private static final Pattern INV_SCORE_PATTERN = Pattern.compile("^([\\w,]+)/([\\w.,]+)$");
    private static final Pattern STORED_SCORE_PATTERN = Pattern.compile("^Stored Dungeon Score: ([\\d,]+)$");

    private int score;
    private boolean scanned = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        String formattedMsg = event.message.getFormattedText();

        Matcher scoreMatcher = SCORE_PATTERN.matcher(msg);
        if (scoreMatcher.find()) {
            score = Integer.parseInt(scoreMatcher.group(1));
            String rank = scoreMatcher.group(2);
            String floor = LocationUtils.floor.name.replaceAll("[()]", "");

            if (rank.equals("S")) score = (int) Math.floor(score * 0.7);
            if (hasDaemon) {
                score *= (int) (1 + daemonLevel / 100.0);
            }

            RngMeterManager.INSTANCE.addScore(floor, score);
        }

        Matcher resetMatcher = RESET_PATTERN.matcher(msg);
        if (resetMatcher.find()) {
            String floor = resetMatcher.group(1);
            RngMeterManager.INSTANCE.setItem(floor, null);
            Utils.modMessage("Reset item on " + floor);
        }

        Matcher setMatcher = SET_PATTERN.matcher(formattedMsg);

        if (setMatcher.matches()) {
            String floor = setMatcher.group(1);
            String item = setMatcher.group(2);
            String formattedItem = item.replace("&", "§");
            RngMeterManager.INSTANCE.setItem(floor, formattedItem);
            Utils.modMessage("Set RNG item " + formattedItem + " §7on " + floor);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (!(Necron.mc.currentScreen instanceof GuiChest)) {
            scanned = false;
            return;
        }

        GuiChest gui = (GuiChest) Necron.mc.currentScreen;
        ContainerChest container = (ContainerChest) gui.inventorySlots;
        IInventory lowerInv = container.getLowerChestInventory();

        String invName = lowerInv.getDisplayName().getUnformattedText();
        if (scanned || !invName.equals("Catacombs RNG Meter")) return;

        if (lowerInv.getSizeInventory() < 35) return;
        scanned = true;

        for (int i = 19; i < 35 && i < lowerInv.getSizeInventory(); i++) {
            ItemStack item = lowerInv.getStackInSlot(i);
            if ((item == null || item.getItem() == null)) continue;
            if (Item.getIdFromItem(item.getItem()) != 397) continue;

            String itemName = item.getDisplayName();
            if (itemName == null || !itemName.replaceAll("§.", "").equals("RNG Meter")) continue;

            processRngMeterItem(item);
        }
    }

    private void processRngMeterItem(ItemStack item) {
        String floor = null;
        String drop = null;
        int current = 0;

        if (item.getTagCompound() == null || !item.getTagCompound().hasKey("display")) return;

        NBTTagCompound display = item.getTagCompound().getCompoundTag("display");
        if (!display.hasKey("Lore")) return;

        NBTTagList loreList = display.getTagList("Lore", 8);
        String[] lore = new String[loreList.tagCount()];
        for (int i = 0; i < loreList.tagCount(); i++) {
            lore[i] = loreList.getStringTagAt(i);
        }

        for (String line : lore) {
            Matcher matcher = FLOOR_PATTERN.matcher(line.replaceAll("§.", ""));
            if (matcher.matches()) {
                floor = matcher.group(1);
                break;
            }
        }

        if (floor == null) return;

        for (int i = 0; i < lore.length; i++) {
            String line = lore[i].replaceAll("§.", "");
            if (line.equals("Selected Drop") && i + 1 < lore.length) {
                drop = lore[i + 1];
                break;
            }
        }

        for (String line : lore) {
            String cleanLine = line.replaceAll("§.", "").trim();

            Matcher scoreMatcher = INV_SCORE_PATTERN.matcher(cleanLine);
            if (scoreMatcher.matches()) {
                String currStr = scoreMatcher.group(1);

                current = Integer.parseInt(currStr.replaceAll(",", ""));
                break;
            }

            Matcher storedMatcher = STORED_SCORE_PATTERN.matcher(cleanLine);
            if (storedMatcher.matches()) {
                current = (int) Double.parseDouble(storedMatcher.group(1).replaceAll(",", ""));
            }
        }

        RngMeterManager.INSTANCE.setScore(floor, current);
        if (drop != null) {
            String formattedDrop = drop.replace("&", "§");
            RngMeterManager.INSTANCE.setItem(floor, formattedDrop);
        } else {
            RngMeterManager.INSTANCE.setItem(floor, null);
        }
    }

    @SubscribeEvent
    public void onGuiClose(GuiOpenEvent event) {
        if (event.gui == null) {
            scanned = false;
        }
    }
}
