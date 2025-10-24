package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.Slayer;
import cn.boop.necron.utils.LocationUtils;
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
    private static final Pattern SLAYER_DROP_PATTERN = Pattern.compile("^[A-Z ]+DROP!? \\(([^)]+)\\) \\(\\+\\d{1,3}% Magic Find\\)$");
    private static final Pattern RNG_RESET_PATTERN = Pattern.compile("§d§lRNG METER! §r§aReselected the (.+?) §afor (.+?)§c! §e§lCLICK HERE §r§ato select a new drop!§r");
    private static final Pattern SET_PATTERN = Pattern.compile("§r§aYou set your §r§d(.+) RNG Meter §r§ato drop (.+)§r§a!§r");
    private static final Pattern INV_SCORE_PATTERN = Pattern.compile("^([\\w,]+)/([\\w.,]+)$");

    private boolean scanned = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0 || !LocationUtils.inSkyBlock) return;
        String message = event.message.getFormattedText();

        Matcher setMatcher = SET_PATTERN.matcher(message);
        Matcher resetMatcher = RNG_RESET_PATTERN.matcher(message);

        if (setMatcher.find()) {
            System.out.println("Set Pattern matched!");
            String slayerType = setMatcher.group(1);
            String setItem = setMatcher.group(2).substring(2);
            System.out.println("Set Item: " + setItem);
            guiNameToType(slayerType, setItem);
        }

        if (resetMatcher.find()) {
            System.out.println("Reset Pattern matched!");
            String currentSlayer = resetMatcher.group(2);
            SlayerRngManager.INSTANCE.setScore(currentSlayer, 0);
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

            System.out.println("Item Name: " + itemName);
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
                System.out.println("Slayer Type: " + slayerType);
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
                System.out.println("Current: " + current);
                break;
            }
        }

        if (slayerType == null) return;

        System.out.println("Setting item: " + slayerType + " = " + drop);
        guiNameToType(slayerType, drop);

        System.out.println("Setting score: " + slayerType + " = " + current);
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
}
