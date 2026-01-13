package cn.boop.necron.utils;

import cn.boop.necron.module.impl.LootProtector;
import cn.boop.necron.module.impl.item.PetInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceUtils {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final Map<String, BazaarItem> bazaarPrices = new HashMap<>();
    private static final Map<String, AuctionItem> auctionPrices = new HashMap<>();
    private static final Map<String, BazaarItem> localBazaarCache = new HashMap<>();
    private static final Map<String, AuctionItem> localAuctionCache = new HashMap<>();

    public static final long UPDATE_INTERVAL = 300;

    public static class BazaarItem {
        private final double instaSell;
        private final double sellOffer;

        public BazaarItem(double instaSell, double sellOffer) {
            this.instaSell = instaSell;
            this.sellOffer = sellOffer;
        }

        public double getInstaSell() {
            return instaSell;
        }

        public double getSellOffer() {
            return sellOffer;
        }

        @Override
        public String toString() {
            return String.format("InstaSell: %.1f, SellOffer: %.1f", instaSell, sellOffer);
        }
    }

    public static class AuctionItem {
        private final double lbin;

        public AuctionItem(double lbin) {
            this.lbin = lbin;
        }

        public double getLbin() {
            return lbin;
        }

        @Override
        public String toString() {
            return String.format("Lowest BIN: %.1f", lbin);
        }
    }

    public static BazaarItem getBazaarItemPrices(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;

        if (localBazaarCache.containsKey(itemId)) return localBazaarCache.get(itemId);

        BazaarItem item = bazaarPrices.get(itemId);
        if (item != null) {
            localBazaarCache.put(itemId, item);
            return item;
        }

        return bazaarPrices.get(itemId);
    }

    public static AuctionItem getAuctionItemPrices(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;

        if (localAuctionCache.containsKey(itemId)) return localAuctionCache.get(itemId);

        AuctionItem item = auctionPrices.get(itemId);
        if (item != null) {
            localAuctionCache.put(itemId, item);
            return item;
        }

        return auctionPrices.get(itemId);
    }

    public static AuctionItem getPetPrice(ItemStack stack) {
        PetInfo petInfo = PetInfo.fromItemStack(stack);
        if (petInfo == null) return null;

        String itemId = petInfo.getItemId();
        if (itemId == null) return null;

        return getAuctionItemPrices(itemId);
    }

    public static AuctionItem getPetPrice(PetInfo petInfo) {
        if (petInfo == null) return null;

        String itemId = petInfo.getItemId();
        if (itemId == null) return null;

        return getAuctionItemPrices(itemId);
    }

    public static double getItemPrice(String itemId, boolean isBazaar) {
        if (isBazaar) {
            BazaarItem bazaarItem = getBazaarItemPrices(itemId);
            return bazaarItem != null ? bazaarItem.getSellOffer() : 0;
        } else {
            AuctionItem auctionItem = getAuctionItemPrices(itemId);
            return auctionItem != null ? auctionItem.getLbin() : 0;
        }
    }

    public static String getItemPrice(LootProtector.PriceType type, String itemId) {
        if (type == LootProtector.PriceType.BAZAAR) {
            return PriceUtils.formatPrice(PriceUtils.getItemPrice(itemId, true));
        } else {
            return PriceUtils.formatPrice(PriceUtils.getItemPrice(itemId, false));
        }
    }

    private static void updateBazaarPrices() {
        localBazaarCache.clear();
        executor.submit(() -> {
            try {
                URL url = new URL("https://api.hypixel.net/skyblock/bazaar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();

                if (!json.get("success").getAsBoolean()) {
                    System.err.println("[PriceUtils] Error loading bazaar data: success = false");
                    return;
                }

                JsonObject products = json.getAsJsonObject("products");
                for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                    String itemId = entry.getKey();
                    JsonObject product = entry.getValue().getAsJsonObject();

                    double instaSell = 0;
                    double sellOffer = 0;

                    JsonArray sellSummary = product.getAsJsonArray("sell_summary");
                    if (sellSummary != null && sellSummary.size() > 0) {
                        instaSell = sellSummary.get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
                    } else {
                        JsonObject quickStatus = product.getAsJsonObject("quick_status");
                        if (quickStatus != null && quickStatus.has("sellPrice")) {
                            instaSell = quickStatus.get("sellPrice").getAsDouble();
                        }
                    }

                    JsonArray buySummary = product.getAsJsonArray("buy_summary");
                    if (buySummary != null && buySummary.size() > 0) {
                        sellOffer = buySummary.get(0).getAsJsonObject().get("pricePerUnit").getAsDouble();
                    } else {
                        JsonObject quickStatus = product.getAsJsonObject("quick_status");
                        if (quickStatus != null && quickStatus.has("buyPrice")) {
                            sellOffer = quickStatus.get("buyPrice").getAsDouble();
                        }
                    }

                    bazaarPrices.put(itemId, new BazaarItem(instaSell, sellOffer));
                }

                System.out.println("[PriceUtils] Bazaar prices updated. Items: " + bazaarPrices.size());

            } catch (Exception e) {
                System.err.println("[PriceUtils] Error loading bazaar data: " + e.getMessage());
            }
        });
    }

    private static void updateAuctionPrices() {
        localAuctionCache.clear();
        executor.submit(() -> {
            try {
                URL url = new URL("https://moulberry.codes/lowestbin.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();

                if (json == null || json.entrySet().isEmpty()) {
                    System.err.println("[PriceUtils] Error loading auctions data: response is empty");
                    return;
                }

                auctionPrices.clear();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    String itemId = entry.getKey();
                    double lowestBin = entry.getValue().getAsDouble();

                    if (lowestBin > 0) {
                        auctionPrices.put(itemId, new AuctionItem(lowestBin));
                    }
                }

                System.out.println("[PriceUtils] Auction prices updated. Items: " + auctionPrices.size());

            } catch (Exception e) {
                System.err.println("[PriceUtils] Error loading auctions data: " + e.getMessage());
            }
        });
    }

    public static String formatPrice(double price) {
        boolean isNegative = price < 0;
        double absPrice = Math.abs(price);

        String formattedPrice;
        if (absPrice >= 1_000_000_000) {
            formattedPrice = String.format("%.1fB", absPrice / 1_000_000_000);
        } else if (absPrice >= 1_000_000) {
            formattedPrice = String.format("%.1fM", absPrice / 1_000_000);
        } else if (absPrice >= 1_000) {
            formattedPrice = String.format("%.1fk", absPrice / 1_000);
        } else {
            formattedPrice = String.format("%.0f", absPrice);
        }

        return isNegative ? "-" + formattedPrice : formattedPrice;
    }

    public static void executePriceQuery(PetInfo petInfo, String itemName, String itemID) {
        if (itemID == null) {
            Utils.modMessage("§c无法识别此物品！");
            return;
        }

        Utils.modMessage("§6=== 价格查询 ===");
        Utils.modMessage("§7物品: §f" + itemName);
        Utils.modMessage("§7ID: §e" + itemID);

        if (petInfo != null) {
            PriceUtils.AuctionItem auctionPrice = PriceUtils.getPetPrice(petInfo);

            Utils.modMessage("§a宠物信息:");
            Utils.modMessage("  §7类型: §f" + petInfo.getType());
            Utils.modMessage("  §7稀有度: ?"/* + getRarityColor(petInfo.getRarity()) + petInfo.getRarity()*/);
            Utils.modMessage("  §7等级: §b" + petInfo.getLevel());

            if (auctionPrice != null && auctionPrice.getLbin() > 0) {
                Utils.modMessage("  §7价格: §6" + PriceUtils.formatPrice(auctionPrice.getLbin()));
            } else {
                Utils.modMessage("  §7价格: §c未找到价格信息");
            }
        } else {
            PriceUtils.BazaarItem bazaarPrice = PriceUtils.getBazaarItemPrices(itemID);

            if (bazaarPrice != null && bazaarPrice.getSellOffer() > 0) {
                Utils.modMessage("§aBazaar价格:");
                Utils.modMessage("  §7即时出售: §6" + PriceUtils.formatPrice(bazaarPrice.getInstaSell()));
                Utils.modMessage("  §7出售订单: §6" + PriceUtils.formatPrice(bazaarPrice.getSellOffer()));
            } else {
                PriceUtils.AuctionItem auctionPrice = PriceUtils.getAuctionItemPrices(itemID);

                if (auctionPrice != null && auctionPrice.getLbin() > 0) {
                    Utils.modMessage("§a拍卖行价格:");
                    Utils.modMessage("  §7最低BIN: §6" + PriceUtils.formatPrice(auctionPrice.getLbin()));
                } else {
                    Utils.modMessage("§c未找到此物品的价格信息");
                }
            }
        }
    }

    public static void initPrices() {
        updateBazaarPrices();
        updateAuctionPrices();
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void forceUpdate() {
        updateBazaarPrices();
        updateAuctionPrices();
    }

    public static int getBazaarDataSize() {
        return bazaarPrices.size();
    }

    public static int getAuctionDataSize() {
        return auctionPrices.size();
    }
}