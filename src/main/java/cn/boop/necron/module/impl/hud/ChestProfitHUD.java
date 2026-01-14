package cn.boop.necron.module.impl.hud;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.item.ItemIdConvertor;
import cn.boop.necron.module.impl.rng.ChestProfit;
import cn.boop.necron.utils.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static cn.boop.necron.config.impl.GUIOptionsImpl.*;

public class ChestProfitHUD {
    private static ChestProfit.ProfitResult currentProfitResult = null;
    private static long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 200;

    public static class RewardChestInfo {
        public final String name;
        public final List<String> contents;

        public RewardChestInfo(String name, List<String> contents) {
            this.name = name;
            this.contents = contents;
        }
    }

    private static class IndexedProfit {
        public final int originalIndex;
        public final ChestProfit.ProfitResult profitResult;

        public IndexedProfit(int originalIndex, ChestProfit.ProfitResult profitResult) {
            this.originalIndex = originalIndex;
            this.profitResult = profitResult;
        }
    }

    public static void onRenderChest(GuiChest guiChest) {
        if (!chestProfit || !isChestProfitApplicable(guiChest)) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            currentProfitResult = ChestProfit.calculateChestProfit(guiChest);
            lastUpdateTime = currentTime;
        }

        if (currentProfitResult == null) return;

        renderProfitHUD(guiChest);
    }

    public static void onRenderDungeonRewardOverview(GuiChest guiChest) {
        if (!chestProfit || !isDungeonRewardOverview(guiChest)) return;

        List<RewardChestInfo> rewardChests = extractRewardChests(guiChest);
        if (rewardChests.isEmpty()) return;

        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        List<ChestProfit.ProfitResult> profits = calculateRewardChestProfits(rewardChests, lowerChest);

        List<IndexedProfit> indexedProfits = new ArrayList<>();
        for (int i = 0; i < profits.size(); i++) {
            indexedProfits.add(new IndexedProfit(i, profits.get(i)));
        }

        indexedProfits.sort((a, b) -> Double.compare(b.profitResult.profit, a.profitResult.profit));

        List<RewardChestInfo> sortedRewardChests = new ArrayList<>();
        List<ChestProfit.ProfitResult> sortedProfits = new ArrayList<>();

        for (IndexedProfit indexed : indexedProfits) {
            sortedRewardChests.add(rewardChests.get(indexed.originalIndex));
            sortedProfits.add(indexed.profitResult);
        }

        renderDungeonRewardOverview(sortedRewardChests, sortedProfits);
    }

    private static void renderProfitHUD(GuiChest guiChest) {
        int guiLeft = (guiChest.width - 176) / 2;
        int guiTop = (guiChest.height - 166) / 2;

        int hudX = guiLeft + 180;
        int hudY = guiTop - 25;

        int[] dimensions = calculateDimensions();
        int hudWidth = dimensions[0];
        int hudHeight = dimensions[1];

        GLUtils.backupAndSetupRender();
        if (chestProfitBg) {
            RenderUtils.drawRoundedRect(
                    hudX, hudY,
                    hudX + hudWidth + 12, hudY + hudHeight + 8,
                    5,
                    cpBgColor.toJavaColor().getRGB()
            );
        }

        drawContent(hudX + 6, hudY + 2, hudWidth);
        GLUtils.restorePreviousRenderState();
    }

    private static void renderDungeonRewardOverview(List<RewardChestInfo> rewardChests, List<ChestProfit.ProfitResult> profits) {
        if (!(Necron.mc.currentScreen instanceof GuiChest)) return;
        GuiChest guiChest = (GuiChest) Necron.mc.currentScreen;
        int guiLeft = (guiChest.width - 176) / 2;
        int guiTop = (guiChest.height - 166) / 2;

        int hudX = guiLeft + 180;
        int hudY = guiTop - 10;

        int[] dimensions = calculateRewardOverviewDimensions(rewardChests, profits);
        int hudWidth = dimensions[0];
        int hudHeight = dimensions[1];

        GLUtils.backupAndSetupRender();
        if (chestProfitBg) {
            RenderUtils.drawRoundedRect(
                    hudX, hudY,
                    hudX + hudWidth + 12, hudY + hudHeight + 8,
                    5,
                    cpBgColor.toJavaColor().getRGB()
            );
        }

        drawRewardOverviewContent(hudX + 6, hudY + 2, hudWidth, rewardChests, profits);
        GLUtils.restorePreviousRenderState();
    }

    private static void drawContent(int startX, int startY, int maxWidth) {
        int currentY = startY;

        String title = "§f§lChest Profit";
        Necron.mc.fontRendererObj.drawString(title,
                startX + (float) (maxWidth - Necron.mc.fontRendererObj.getStringWidth(title)) / 2,
                currentY, Color.WHITE.getRGB(), true);
        currentY += 12;

        String valueLine = "§eValue (BIN): §6" + formatCoins(currentProfitResult.totalValue) + " coins";
        Necron.mc.fontRendererObj.drawString(valueLine, startX, currentY, Color.WHITE.getRGB(), true);
        currentY += 9;

        if (currentProfitResult.isRerolled) {
            double featherPrice = PriceUtils.getItemPrice("KISMET_FEATHER", true);
            String featherLine = "§eKismet Feather: §2" + formatCoins(featherPrice) + " coins";
            Necron.mc.fontRendererObj.drawString(featherLine, startX, currentY, Color.WHITE.getRGB(), true);
            currentY += 9;
        }

        String profitColor = currentProfitResult.profit >= 0 ? "§2" : "§c";
        String profitPrefix = currentProfitResult.profit >= 0 ? "+" : "-";
        String profitLine = "§eProfit/Loss: " + profitColor + profitPrefix +
                formatCoins(Math.abs(currentProfitResult.profit)) + " coins";
        Necron.mc.fontRendererObj.drawString(profitLine, startX, currentY, Color.WHITE.getRGB(), true);
        currentY += 15;

        for (ChestProfit.ItemDetail detail : currentProfitResult.itemDetails) {
            drawItemLine(detail, startX, currentY, maxWidth);
            currentY += 9;
        }
    }

    private static void drawRewardOverviewContent(int startX, int startY, int maxWidth, List<RewardChestInfo> rewardChests, List<ChestProfit.ProfitResult> profits) {
        int currentY = startY;

        String title = "§f§lAll Chest Profits";
        Necron.mc.fontRendererObj.drawString(title,
                startX + (float) (maxWidth - Necron.mc.fontRendererObj.getStringWidth(title)) / 2,
                currentY, Color.WHITE.getRGB(), true);
        currentY += 15;

        for (int i = 0; i < Math.min(profits.size(), rewardChests.size()); i++) {
            ChestProfit.ProfitResult result = profits.get(i);
            String chestName = rewardChests.get(i).name;
            String profitColor = result.profit >= 0 ? "§2" : "§c";
            String profitPrefix = result.profit >= 0 ? "+" : "-";
            String profitLine = chestName + ": " + profitColor + profitPrefix +
                    formatCoins(Math.abs(result.profit)) + " coins";
            Necron.mc.fontRendererObj.drawString(profitLine, startX, currentY, Color.WHITE.getRGB(), true);
            currentY += 9;
        }
    }

    private static void drawItemLine(ChestProfit.ItemDetail detail, int x, int y, int maxWidth) {
        String itemName = detail.displayName;

        if (detail.apiId != null && (detail.apiId.contains("ESSENCE") || itemName.toLowerCase().contains("essence"))) {
            itemName = itemName.replace("§8x" + detail.count, "").trim();
            itemName += " §8x" + detail.count;
        }

        String valueStr = "§2" + formatCoins(detail.totalValue);
        String fullLine = itemName + " " + valueStr;

        int lineWidth = Necron.mc.fontRendererObj.getStringWidth(fullLine);
        if (lineWidth > maxWidth) {
            int valueWidth = Necron.mc.fontRendererObj.getStringWidth(valueStr);
            int maxNameWidth = maxWidth - valueWidth - 10;

            String truncatedName = truncateString(itemName, maxNameWidth);
            fullLine = truncatedName + " " + valueStr;
        }

        Necron.mc.fontRendererObj.drawString(fullLine, x, y, Color.WHITE.getRGB(), true);
    }

    private static String truncateString(String text, int maxWidth) {
        if (Necron.mc.fontRendererObj.getStringWidth(text) <= maxWidth) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            result.append(c);
            if (Necron.mc.fontRendererObj.getStringWidth(result + "...") > maxWidth) {
                return result.substring(0, Math.max(0, result.length() - 1)) + "...";
            }
        }
        return text;
    }

    private static int[] calculateDimensions() {
        int maxWidth = 150;
        int height = 14;

        if (currentProfitResult != null) {
            String title = "§f§lChest Profit";
            maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(title));

            String valueLine = "§5Value (BIN): §b" + formatCoins(currentProfitResult.totalValue) + " §6Coins";
            maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(valueLine));

            String profitLine = "§5Profit/Loss: " +
                    (currentProfitResult.profit >= 0 ? "§a+" : "§c") +
                    formatCoins(Math.abs(currentProfitResult.profit)) + " §6Coins";
            maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(profitLine));

            if (currentProfitResult.isRerolled) {
                double featherPrice = PriceUtils.getItemPrice("KISMET_FEATHER", true);
                String featherLine = "§5Kismet Feather: §b" + formatCoins(featherPrice) + " §6Coins";
                maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(featherLine));
            }

            for (ChestProfit.ItemDetail detail : currentProfitResult.itemDetails) {
                String itemName = detail.displayName;
                if (detail.apiId != null && (detail.apiId.contains("ESSENCE") || itemName.toLowerCase().contains("essence"))) {
                    itemName = itemName.replace("§8x" + detail.count, "").trim();
                    itemName += " §8x" + detail.count;
                }
                String itemLine = itemName + " §7" + formatCoins(detail.totalValue);
                maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(itemLine));
            }

            height += 9;
            if (currentProfitResult.isRerolled) height += 9;
            height += 9;
            height += 2;
            height += currentProfitResult.itemDetails.size() * 9;
        }

        return new int[]{maxWidth, height};
    }

    private static int[] calculateRewardOverviewDimensions(List<RewardChestInfo> rewardChests, List<ChestProfit.ProfitResult> profits) {
        int maxWidth = 150;
        int height = 14;

        String title = "§f§lAll Chest Profits";
        maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(title));

        for (int i = 0; i < Math.min(profits.size(), rewardChests.size()); i++) {
            String chestName = rewardChests.get(i).name;
            String profitLine = chestName + ": " + (profits.get(i).profit >= 0 ? "§2+" : "§c") +
                    formatCoins(Math.abs(profits.get(i).profit)) + " coins";
            maxWidth = Math.max(maxWidth, Necron.mc.fontRendererObj.getStringWidth(profitLine));
        }

        height += profits.size() * 9;

        return new int[]{maxWidth, height};
    }

    private static boolean isChestProfitApplicable(GuiChest guiChest) {
        if (guiChest == null || guiChest.inventorySlots == null) return false;

        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();

        String chestName = lowerChest.getDisplayName().getUnformattedText();
        return "Wood Chest".equals(chestName) ||
                "Gold Chest".equals(chestName) ||
                "Diamond Chest".equals(chestName) ||
                "Emerald Chest".equals(chestName) ||
                "Obsidian Chest".equals(chestName) ||
                "Bedrock Chest".equals(chestName);
    }

    private static List<RewardChestInfo> extractRewardChests(GuiChest guiChest) {
        List<RewardChestInfo> rewardChests = new ArrayList<>();
        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();

        for (int slot = 9; slot <= 17; slot++) {
            if (slot < lowerChest.getSizeInventory()) {
                ItemStack itemStack = lowerChest.getStackInSlot(slot);
                if (itemStack != null && itemStack.getItem() == Items.skull) {
                    String chestName = itemStack.getDisplayName();

                    List<String> lore = ItemUtils.getItemLore(itemStack);
                    List<String> contents = extractContentsFromLore(lore);
                    rewardChests.add(new RewardChestInfo(chestName, contents));
                }
            }
        }

        return rewardChests;
    }

    private static List<String> extractContentsFromLore(List<String> lore) {
        List<String> contents = new ArrayList<>();
        boolean foundContents = false;

        for (String line : lore) {
            String cleanLine = Utils.removeFormatting(line);
            if (foundContents) {
                if (cleanLine.trim().isEmpty()) break;
                contents.add(line);
            } else if (cleanLine.equals("Contents")) {
                foundContents = true;
            }
        }

        return contents;
    }

    private static List<ChestProfit.ProfitResult> calculateRewardChestProfits(List<RewardChestInfo> rewardChests, IInventory lowerChest) {
        List<ChestProfit.ProfitResult> results = new ArrayList<>();

        for (RewardChestInfo chestInfo : rewardChests) {
            double cost = ChestProfit.getChestCost(lowerChest);
            boolean needKey = ChestProfit.getChestCost(lowerChest) == -2;

            double totalValue = 0;
            int itemCount = 0;
            List<ChestProfit.ItemDetail> itemDetails = new ArrayList<>();

            for (String contentLine : chestInfo.contents) {
                String cleanLine = Utils.removeFormatting(contentLine);

                if (!cleanLine.trim().isEmpty() && !cleanLine.equals("Contents")) {
                    String apiId = ItemIdConvertor.convertDisplayNameToApiId(cleanLine);

                    double itemPrice = 0;
                    PriceUtils.AuctionItem auctionPrice = PriceUtils.getAuctionItemPrices(apiId);
                    if (auctionPrice != null && auctionPrice.getLbin() > 0) {
                        itemPrice = auctionPrice.getLbin();
                    } else {
                        PriceUtils.BazaarItem bazaarPrice = PriceUtils.getBazaarItemPrices(apiId);
                        if (bazaarPrice != null && bazaarPrice.getSellOffer() > 0) {
                            itemPrice = bazaarPrice.getSellOffer();
                        }
                    }

                    if (itemPrice > 0) {
                        totalValue += itemPrice;
                        itemCount++;
                        itemDetails.add(new ChestProfit.ItemDetail(null, cleanLine, apiId, itemPrice, 1));
                    }
                }
            }

            double profit = totalValue - cost;
            if (needKey) profit -= PriceUtils.getItemPrice("DUNGEON_CHEST_KEY", true);

            results.add(new ChestProfit.ProfitResult(totalValue, 0, profit, itemCount, itemDetails, false, false, false));
        }

        return results;
    }

    public static boolean isDungeonRewardOverview(GuiChest guiChest) {
        if (guiChest == null || guiChest.inventorySlots == null) return false;

        ContainerChest container = (ContainerChest) guiChest.inventorySlots;
        IInventory lowerChest = container.getLowerChestInventory();
        String chestName = lowerChest.getDisplayName().getUnformattedText();

        return chestName.contains("Catacombs - ") || chestName.contains("Kuudra");
    }

    private static String formatCoins(double coins) {
        long roundedCoins = Math.round(coins);
        return Utils.addNumSeparator((int)roundedCoins);
    }
}