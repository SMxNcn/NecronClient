package cn.boop.necron.module.impl.rng;

import cn.boop.necron.module.impl.item.EnumRarity;
import cn.boop.necron.module.impl.item.ItemIdConvertor;
import cn.boop.necron.module.impl.item.PetInfo;
import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.PriceUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestProfit {
    private static final Pattern COST_PATTERN = Pattern.compile("([\\d,]+(?:\\.\\d+)?) Coins?");

    public static class ProfitResult {
        public final double totalValue;
        public final double cost;
        public final boolean isOpened;
        public final boolean isRerolled;
        public final boolean needKey;
        public final double profit;
        public final int itemCount;
        public final List<ItemDetail> itemDetails;

        public ProfitResult(double totalValue, double cost, double profit, int itemCount, List<ItemDetail> itemDetails, boolean isOpened, boolean isRerolled, boolean needKey) {
            this.totalValue = totalValue;
            this.cost = cost;
            this.profit = profit;
            this.itemCount = itemCount;
            this.itemDetails = itemDetails;
            this.isOpened = isOpened;
            this.isRerolled = isRerolled;
            this.needKey = needKey;
        }

        @Override
        public String toString() {
            return String.format("Value: %s, Cost: %s, Profit: %s, Item(s): %d",
                    PriceUtils.formatPrice(totalValue),
                    cost > 0 ? ("+" + PriceUtils.formatPrice(cost)) : ("-" + PriceUtils.formatPrice(cost)),
                    PriceUtils.formatPrice(profit),
                    itemCount);
        }
    }

    public static class ItemDetail {
        public final String displayName;
        public final String apiId;
        public final double price;
        public final int count;
        public final double totalValue;

        public ItemDetail(ItemStack itemStack, String displayName, String apiId, double price, int count) {
            this.displayName = getBookDisplayName(itemStack, displayName, apiId);
            this.apiId = apiId;
            this.price = price;
            this.count = count;
            this.totalValue = price * count;
        }
    }

    public static ProfitResult calculateChestProfit(GuiChest guiChest) {
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();

        double cost = getChestCost(lowerChest);
        boolean isOpened = getChestCost(lowerChest) == -1;
        boolean isRerolled = isChestRerolled(lowerChest);
        boolean needKey = getChestCost(lowerChest) == -2;

        double totalValue = 0;
        int itemCount = 0;
        List<ItemDetail> itemDetails = new ArrayList<>();

        for (int i = 0; i < lowerChest.getSizeInventory(); i++) {
            ItemStack itemStack = lowerChest.getStackInSlot(i);

            if (i == 31) continue;

            if (itemStack != null) {
                String apiId;
                PetInfo petInfo = PetInfo.fromItemStack(itemStack);
                if (petInfo != null) {
                    apiId = petInfo.getItemId();
                } else {
                    apiId = ItemIdConvertor.getApiIdFromItemStack(itemStack);
                }

                String displayName = itemStack.getDisplayName();

                if (apiId != null && !apiId.isEmpty()) {
                    double itemPrice;
                    double itemTotalValue;

                    if (displayName.toLowerCase().contains("essence")) {
                        PriceUtils.BazaarItem bazaarPrice = PriceUtils.getBazaarItemPrices(apiId);
                        if (bazaarPrice != null && bazaarPrice.getSellOffer() > 0) {
                            int essenceCount = getEssenceCount(displayName);
                            itemPrice = bazaarPrice.getSellOffer();
                            itemTotalValue = itemPrice * essenceCount;
                            totalValue += itemTotalValue;
                            itemCount++;

                            itemDetails.add(new ItemDetail(itemStack, displayName, apiId, itemPrice, essenceCount));
                        }
                    } else {
                        PriceUtils.AuctionItem auctionPrice = PriceUtils.getAuctionItemPrices(apiId);
                        if (auctionPrice != null && auctionPrice.getLbin() > 0) {
                            itemPrice = auctionPrice.getLbin();
                            itemTotalValue = itemPrice;
                            totalValue += itemTotalValue;
                            itemCount++;
                            itemDetails.add(new ItemDetail(itemStack, displayName, apiId, itemPrice, 1));
                        } else {
                            PriceUtils.BazaarItem bazaarPrice = PriceUtils.getBazaarItemPrices(apiId);
                            if (bazaarPrice != null && bazaarPrice.getSellOffer() > 0) {
                                itemPrice = bazaarPrice.getSellOffer();
                                itemTotalValue = itemPrice;
                                totalValue += itemTotalValue;
                                itemCount++;
                                itemDetails.add(new ItemDetail(itemStack, displayName, apiId, itemPrice, 1));
                            }
                        }
                    }
                }
            }
        }

        double profit = totalValue - cost;
        if (isRerolled) profit -= PriceUtils.getItemPrice("KISMET_FEATHER", true);
        if (needKey) profit -= PriceUtils.getItemPrice("DUNGEON_CHEST_KEY", true);

        return new ProfitResult(totalValue, cost, profit, itemCount, itemDetails, isOpened, isRerolled, needKey);
    }

    public static double getChestCost(IInventory chest) {
        ItemStack costItem = chest.getStackInSlot(31);

        if (costItem != null) {
            String displayName = costItem.getDisplayName();

            if (displayName.contains("Open Reward Chest")) {
                List<String> lore = ItemUtils.getItemLore(costItem);

                for (String loreLine : lore) {
                    if (loreLine.contains("Cost")) {
                        int costIndex = lore.indexOf(loreLine) + 1;
                        if (costIndex < lore.size()) {
                            String costLine = Utils.removeFormatting(lore.get(costIndex));
                            return (parseCost(costLine));
                        }

                        if (lore.get(costIndex + 1).equals("Dungeon Chest Key")) return -1;
                        if (lore.get(lore.size() - 1).equals("Already opened!")) return -2;
                    }
                }
            }
        }

        return 0;
    }

    private static double parseCost(String costLine) {
        Matcher matcher = COST_PATTERN.matcher(costLine);
        if (matcher.find()) {
            String costStr = matcher.group(1);
            return Double.parseDouble(costStr.replace(",", ""));
        }

        return 0;
    }

    public static void displayChestProfit(GuiChest guiChest) {
        ProfitResult result = calculateChestProfit(guiChest);

        StringBuilder sb = new StringBuilder();
        sb.append(" §7Price/BIN: §6").append(PriceUtils.formatPrice(result.totalValue)).append("\n");
        sb.append(" §7Cost: §6").append(result.cost == -1 ? sb.append("Opened!") : PriceUtils.formatPrice(result.cost)).append("\n");
        sb.append(" §7Profit/Lost: ").append((result.profit >= 0 ? "§a+" : "§c")).append(PriceUtils.formatPrice(result.profit)).append("\n");
        if (result.cost == -2) sb.append(" §5Kismet Feather: ").append(PriceUtils.getItemPrice("KISMET_FEATHER", true));
        sb.append(" §7Item(s): §e").append(result.itemCount).append(" §7\n\n");

        for (int i = 0; i < result.itemDetails.size(); i++) {
            ItemDetail detail = result.itemDetails.get(i);
            String itemInfo = String.format("§7%s §7: §6%s", detail.displayName, PriceUtils.formatPrice(detail.totalValue));
            sb.append(itemInfo);

            if (i < result.itemDetails.size() - 1) {
                sb.append("\n");
            }
        }

        Utils.modMessage(sb.toString());
    }

    private static int getEssenceCount(String displayName) {
        Pattern essenceCountPattern = Pattern.compile(".*§8x(\\d+).*");
        Matcher matcher = essenceCountPattern.matcher(displayName);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    private static boolean isChestRerolled(IInventory chest) {
        if (chest == null) return false;

        ItemStack itemStack = chest.getStackInSlot(50);
        if (itemStack == null || itemStack.getItem() != Items.feather) return false;
        String itemName = Utils.removeFormatting(itemStack.getDisplayName());
        List<String> lore = ItemUtils.getItemLore(itemStack);

        return itemName.equals("Reroll Chest") && Utils.removeFormatting(lore.get(lore.size() - 1)).contains("You already rerolled a chest!");
    }

    private static String getBookDisplayName(ItemStack itemStack, String originalDisplayName, String apiId) {
        if (apiId != null && apiId.startsWith("ENCHANTMENT_")) {
            List<String> lore = ItemUtils.getItemLore(itemStack);
            if (lore != null) {
                for (String loreLine : lore) {
                    if (EnumRarity.RARITY_PATTERN.matcher(loreLine).find()) {
                        EnumRarity rarity = EnumRarity.parseRarity(loreLine);
                        String enchantName = ItemUtils.getItemLoreLine(itemStack, 1);
                        if (enchantName != null) {
                            return rarity.getColorCode() + Utils.removeFormatting(enchantName);
                        }
                    }
                }
            }

            String loreLine = ItemUtils.getItemLoreLine(itemStack, 1);
            if (loreLine != null) return loreLine;
        }
        return originalDisplayName;
    }
}
