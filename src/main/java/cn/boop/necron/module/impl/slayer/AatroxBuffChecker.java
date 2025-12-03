package cn.boop.necron.module.impl.slayer;

import cn.boop.necron.Necron;
import cn.boop.necron.config.impl.GUIOptionsImpl;
import cn.boop.necron.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AatroxBuffChecker {
    private static boolean isAatrox = false;
    private static boolean isInitialized = false;

    public static void initialize() {
        if (!isInitialized) {
            try {
                String apiResponse = fetchElectionData();
                if (apiResponse != null) {
                    isAatrox = hasAatroxSlayerXPBuff(apiResponse);
                    isInitialized = true;
                    Necron.LOGGER.info("Aatrox is {}", isAatrox ? "active" : "inactive");
                }
            } catch (Exception e) {
                Necron.LOGGER.error("Failed to initialize Aatrox buff checker: {}", e.getMessage());
            }
        }
    }

    private static boolean hasAatroxSlayerXPBuff(String apiResponse) {
        try {
            JsonObject json = new JsonParser().parse(apiResponse).getAsJsonObject();

            if (!json.get("success").getAsBoolean()) {
                return false;
            }

            JsonObject mayor = json.getAsJsonObject("mayor");

            if (mayor.has("name") && "Aatrox".equals(mayor.get("name").getAsString())) {
                return checkPerksForSlayerXPBuff(mayor.getAsJsonArray("perks"));
            }

            if (mayor.has("minister")) {
                JsonObject minister = mayor.getAsJsonObject("minister");
                if (minister.has("name") && "Aatrox".equals(minister.get("name").getAsString())) {
                    if (minister.has("perk")) {
                        JsonObject perk = minister.getAsJsonObject("perk");
                        return perk.has("name") &&
                                perk.get("name").getAsString().toLowerCase().contains("slayer xp buff");
                    }
                }
            }

        } catch (Exception e) {
            Necron.LOGGER.error("Error checking Aatrox's Slayer XP Buff: {}", e.getMessage());
        }
        return false;
    }

    private static boolean checkPerksForSlayerXPBuff(JsonArray perks) {
        if (perks == null) return false;

        for (JsonElement perkElement : perks) {
            JsonObject perk = perkElement.getAsJsonObject();
            if (perk.has("name")) {
                String perkName = perk.get("name").getAsString().toLowerCase();
                if (perkName.contains("slayer xp buff")) {
                    isAatrox = true;
                    Utils.modMessage("§aAatrox §7has §d§lSlayer XP Buff§7!");
                    Necron.LOGGER.info("Aatrox has Slayer XP Buff!");
                    return true;
                }
            }
        }
        return false;
    }

    public static String fetchElectionData() throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("https://api.hypixel.net/v2/resources/skyblock/election");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } else {
                Necron.LOGGER.warn("API request failed with status: {}", connection.getResponseCode());
            }
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) { /* ignore */ }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static double getSlayerXPMultiplier() {
        double multiplier = 1.0;
        if (isAatrox) multiplier *= 1.25;
        if (GUIOptionsImpl.hasDaemon) multiplier *= 1.0 + (GUIOptionsImpl.daemonLevel * 0.01);

        return multiplier;
    }
}
