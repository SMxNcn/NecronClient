package cn.boop.necron.module.impl.item;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.boop.necron.config.impl.NecronOptionsImpl.itemProtector;

public class ItemProtector {
    private static final Set<String> PROTECTED_UUIDS = new HashSet<>();

    public static void initUuids() {
        loadFromFile();
    }

    public static void loadFromFile() {
        Set<String> loaded = JsonUtils.loadProtectedItems();
        PROTECTED_UUIDS.clear();
        PROTECTED_UUIDS.addAll(loaded);
        Necron.LOGGER.info("Loaded {} protected items from file", PROTECTED_UUIDS.size());
    }

    public static void saveToFile() {
        JsonUtils.saveProtectedItems(new HashSet<>(PROTECTED_UUIDS));
    }

    public static void toggleHoveredItemProtection() {
        if (!(Necron.mc.currentScreen instanceof GuiContainer)) return;

        GuiContainer gui = (GuiContainer) Necron.mc.currentScreen;
        Slot hoveredSlot = gui.getSlotUnderMouse();

        if (hoveredSlot == null || !hoveredSlot.getHasStack()) return;

        ItemStack hoveredItem = hoveredSlot.getStack();
        String uuid = ItemUtils.getItemUUID(hoveredItem);

        if (uuid == null || uuid.isEmpty()) {
            Utils.modMessage("§cYou can't protect an item without UUID!");
            return;
        }

        String itemName = hoveredItem.getDisplayName();
        if (Utils.removeFormatting(itemName).contains("AUCTION FOR ITEM:")) itemName = ItemUtils.getItemLoreLine(hoveredItem, 3);
        if (itemName.contains(" x1")) itemName = itemName.replaceAll(" §8x\\d+§c$", "").replaceAll(" x\\d+$", "");

        boolean isProtected = PROTECTED_UUIDS.contains(uuid);

        if (isProtected) {
            PROTECTED_UUIDS.remove(uuid);
            Utils.modMessage("§cI will no longer protect your " + itemName + "§r§c!");
        } else {
            PROTECTED_UUIDS.add(uuid);
            Utils.modMessage("§aI will now protect your " + itemName + "§r§a!");
        }

        saveToFile();
    }

    public static boolean isItemProtected(ItemStack itemStack) {
        if (!itemProtector || itemStack == null) return false;

        String uuid = ItemUtils.getItemUUID(itemStack);
        return uuid != null && !uuid.isEmpty() && PROTECTED_UUIDS.contains(uuid);
    }

    public static boolean shouldPreventDrop(ItemStack itemStack) {
        return itemProtector && isItemProtected(itemStack);
    }

    public static boolean shouldPreventGuiClick(GuiChest guiChest, ItemStack itemStack) {
        if (!itemProtector || !isItemProtected(itemStack)) return false;

        GuiType guiType = getGuiType(guiChest);
        System.out.println("Gui Type: " + guiType.toString());
        return guiType != GuiType.UNKNOWN;
    }

    public static GuiType getGuiType(GuiChest guiChest) {
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        String chestName = lowerChest.getDisplayName().getUnformattedText();

        if (chestName.contains("Salvage Items")) return GuiType.SALVAGE;
        if (chestName.contains("Create BIN Auction") || chestName.contains("Create Auction") || chestName.contains("Auction House")) return GuiType.AUCTION;
        if (chestName.contains("Trades") || chestName.contains("Booster Cookie")) return GuiType.SELL;
        if (lowerChest.getSizeInventory() > 10) {
            ItemStack slotStack = lowerChest.getStackInSlot(9);
            if (slotStack != null && slotStack.hasTagCompound()) {
                List<String> lore = ItemUtils.getItemLore(slotStack);
                if (lore != null && lore.stream().anyMatch(loreLine -> loreLine.contains("Click to trade!"))) return GuiType.SELL;
            }
        }
        return GuiType.UNKNOWN;
    }

    public static void sendProtectMessage(ItemStack itemStack, GuiType guiType) {
        if (itemProtector && isItemProtected(itemStack)) {
            String itemName = itemStack.getDisplayName();
            String clean = Utils.removeFormatting(itemName);
            if (clean.contains("AUCTION FOR ITEM:")) itemName = ItemUtils.getItemLoreLine(itemStack, 3);
            if (clean.contains(" x1")) itemName = itemName.replaceAll(" §8x\\d+§c$", "").replaceAll(" §8x\\d+$", "").replaceAll(" x\\d+$", "");
            Utils.modMessage(guiType.getMessageFormat(itemName));
            Necron.mc.thePlayer.playSound("note.bass", 0.9F, 0.65F);
        }
    }
}
