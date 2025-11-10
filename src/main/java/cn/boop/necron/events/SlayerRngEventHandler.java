package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.Slayer;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerRngEventHandler {
    private static final Pattern SLAYER_DROP_PATTERN = Pattern.compile("^§r§.§l[A-Z ]+DROP!? §r§7\\(§r§f§r(.*?)§r§7\\)");
    private static final Pattern RNG_RESET_PATTERN = Pattern.compile("§d§lRNG METER! §r§aReselected the (.+?) §afor §c(.+?)§a! §e§lCLICK HERE §r§ato select a new drop!§r");
    private static final Pattern SET_PATTERN = Pattern.compile("§r§aYou set your §r§d(.+) RNG Meter §r§ato drop (.+)§r§a!§r");
    private static final Pattern INV_SCORE_PATTERN = Pattern.compile("^([\\w,]+)/([\\w.,]+)$");
    private static final Pattern STORED_XP_PATTERN = Pattern.compile("§dRNG Meter §f- §d(\\d+) Stored XP§r");

    private boolean scanned = false;
    private static int resetScore = 0;
    private static double percentage = 0;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0 || !LocationUtils.inSkyBlock) return;
        String message = event.message.getFormattedText();

        Matcher setMatcher = SET_PATTERN.matcher(message);
        Matcher dropMatcher = SLAYER_DROP_PATTERN.matcher(message);
        Matcher resetMatcher = RNG_RESET_PATTERN.matcher(message);
        Matcher storedXpMatcher = STORED_XP_PATTERN.matcher(message);

        if (resetMatcher.find()) {
            String currentItem = resetMatcher.group(1);
            String currentSlayer = resetMatcher.group(2);
            String selectedDrop = SlayerRngManager.INSTANCE.getItem(currentSlayer);
            resetScore = SlayerRngManager.INSTANCE.getScore(currentSlayer);
            percentage = SlayerRngManager.INSTANCE.getMeterPercentage(SlayerEventHandler.getCurrentSlayer().getDisplayName());

            if (selectedDrop != null && selectedDrop.equals(currentItem)) {
                SlayerRngManager.INSTANCE.setScore(currentSlayer, 0);
            }
            return;
        }

        if (dropMatcher.find()) {
            String droppedItem = dropMatcher.group(1);

            Slayer slayer = SlayerEventHandler.getCurrentSlayer();
            if (slayer != Slayer.Unknown) {
                String selectedItem = SlayerRngManager.INSTANCE.getItem(slayer.getDisplayName());

                if (selectedItem != null && selectedItem.equals(droppedItem)) {
                    int score = SlayerRngEventHandler.getResetScore();
                    Utils.modMessage("§dRng Item §7reset! (§6" + Utils.addNumSeparator(score) + " §bScore, §6" + String.format("%.2f", percentage) + "§b%§7)");
                }
            }
            return;
        }

        if (storedXpMatcher.find()) {
            int storedXp = Integer.parseInt(storedXpMatcher.group(1));
            SlayerRngManager.INSTANCE.setScore(SlayerEventHandler.getCurrentSlayer().getDisplayName(), storedXp);
            return;
        }


        if (setMatcher.find()) {
            String slayerType = setMatcher.group(1);
            String setItem = setMatcher.group(2).substring(2);
            guiNameToType(slayerType, setItem);
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

        if (scanned || !invName.equals("Slayer RNG Meter")) return;

        if (lowerInv.getSizeInventory() < 35) return;
        scanned = true;

        for (int i = 19; i < 35 && i < lowerInv.getSizeInventory(); i++) {
            ItemStack item = lowerInv.getStackInSlot(i);
            if ((item == null || item.getItem() == null)) continue;

            String itemName = item.getDisplayName();
            if (itemName == null || !itemName.replaceAll("§.", "").equals("RNG Meter")) continue;
            processSlayerRngMeterItem(item);
        }
    }

    private void processSlayerRngMeterItem(ItemStack item) {
        String slayerType = null;
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
            String cleanLine = line.replaceAll("§.", "");
            if (isValidSlayerType(cleanLine)) {
                slayerType = getSlayerByGuiName(cleanLine);
                break;
            }
        }

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
        }

        if (slayerType == null) return;

        SlayerRngManager.INSTANCE.setItem(slayerType, drop);
        SlayerRngManager.INSTANCE.setScore(slayerType, current);
    }

    private static String getSlayerByGuiName(String guiName) {
        switch(guiName) {
            case "Revenant Horror": return Slayer.Revenant.getDisplayName();
            case "Sven Packmaster": return Slayer.Sven.getDisplayName();
            case "Tarantula Broodfather": return Slayer.Tarantula.getDisplayName();
            case "Voidgloom Seraph": return Slayer.Voidgloom.getDisplayName();
            case "Riftstalker Bloodfiend": return Slayer.Riftstalker.getDisplayName();
            case "Inferno Demonlord": return Slayer.Inferno.getDisplayName();
            default: return null;
        }
    }

    private static void guiNameToType(String guiName, String setItem) {
        String slayerDisplayName = getSlayerByGuiName(guiName);
        if (slayerDisplayName != null) {
            SlayerRngManager.INSTANCE.setItem(slayerDisplayName, setItem);
        }
    }

    private boolean isValidSlayerType(String loreName) {
        switch(loreName) {
            case "Revenant Horror":
            case "Sven Packmaster":
            case "Tarantula Broodfather":
            case "Voidgloom Seraph":
            case "Riftstalker Bloodfiend":
            case "Inferno Demonlord":
                return true;
            default:
                return false;
        }
    }

    @SubscribeEvent
    public void onGuiClose(GuiOpenEvent event) {
        if (event.gui == null) {
            scanned = false;
        }
    }

    public static int getResetScore() {
        return resetScore;
    }
}
