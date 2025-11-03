package cn.boop.necron.module.impl.rng;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.hud.RngMeterHUD;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public abstract class RngManager {
    protected final Map<String, RngMeterHUD.RngMeterData> meters = new HashMap<>();
    protected final Map<String, Map<String, Integer>> rngMeterValues = new HashMap<>();
    protected File dataFile;
    protected final Gson gson = new Gson();

    public static class RngMeterSaveData {
        public Map<String, RngMeterUserData> dungeonData = new HashMap<>();
        public Map<String, RngMeterUserData> slayerData = new HashMap<>();

        public static class RngMeterUserData {
            public int score;
            public String item;
            Integer needed;

            public RngMeterUserData(int score, String item, Integer needed) {
                this.score = score;
                this.item = item;
                this.needed = needed;
            }
        }
    }

    protected void checkDataExists(String key) {
        if (!meters.containsKey(key)) {
            meters.put(key, new RngMeterHUD.RngMeterData(key, "", 0, 0));
        } else {
            RngMeterHUD.RngMeterData data = meters.get(key);
            if (data.item != null && !data.item.isEmpty()
                    && data.needed <= 0
                    && rngMeterValues.containsKey(key)
                    && rngMeterValues.get(key).containsKey(data.item)) {
                data.needed = rngMeterValues.get(key).get(data.item);
            }
        }
    }

    public static Map<String, Map<String, Integer>> loadRngMeterValues() {
        Gson gson = new Gson();
        try (InputStream inputStream = RngManager.class.getResourceAsStream("/RNGMeterValues.json")) {
            if (inputStream != null) {
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                Type type = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
                return gson.fromJson(reader, type);
            } else {
                Necron.LOGGER.error("Failed to load RNGMeterValues from resources: InputStream is null");
            }
        } catch (Exception e) {
            Necron.LOGGER.error("Failed to load RNGMeterValues from resources: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    public void setScore(String key, int score) {
        checkDataExists(key);
        meters.get(key).score = score;
        onSave();
    }

    public int getScore(String key) {
        checkDataExists(key);
        return meters.get(key).score;
    }

    public void setItem(String key, String item) {
        checkDataExists(key);
        RngMeterHUD.RngMeterData data = meters.get(key);
        data.item = item;

        Necron.LOGGER.info("Setting item for {}: {}", key, item);

        if (item == null || item.isEmpty()) {
            data.needed = 0;
        } else if (rngMeterValues.containsKey(key) && rngMeterValues.get(key).containsKey(item)) {
            data.needed = rngMeterValues.get(key).get(item);
            Necron.LOGGER.info("Set needed value for {}: {}", key, data.needed);
        } else {
            Necron.LOGGER.warn("No needed value found for {} item: {}", key, item);
            Necron.LOGGER.info("Available keys: {}", rngMeterValues.keySet());
            if (rngMeterValues.containsKey(key)) {
                Necron.LOGGER.info("Available items for {}: {}", key, rngMeterValues.get(key).keySet());
            }
        }
        onSave();
    }

    public String getItem(String key) {
        checkDataExists(key);
        return meters.get(key).item;
    }

    public void loadValuesFile(Map<String, Map<String, Integer>> values) {
        rngMeterValues.clear();
        rngMeterValues.putAll(values);
    }

    public RngMeterHUD.RngMeterData getMeter(String key) {
        checkDataExists(key);
        return meters.get(key);
    }

    public double getMeterPercentage(String key) {
        checkDataExists(key);
        RngMeterHUD.RngMeterData data = meters.get(key);
        if (data == null || data.needed <= 0) return 0.0;
        double percentage = (double) data.score / data.needed * 100;
        return Math.min(percentage, 100.0);
    }

    public String getMeterBar(String key) {
        checkDataExists(key);
        RngMeterHUD.RngMeterData data = meters.get(key);
        if (data == null) return "";
        return generateMeterBar(data.score, data.needed);
    }

    public String generateMeterBar(int score, int needed) {
        int bars = 15;
        double percentage = needed <= 0 ? 0 : Math.min((double) score / needed, 1.0);
        int progress = (int) Math.floor(bars * percentage);

        return "§d" + String.format("%,d", score) + " §a§m§l" + repeat(progress)
                + "§7§m§l" + repeat(bars - progress) + "§r §d" + String.format("%,d", needed);
    }

    private String repeat(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(" ");
        return sb.toString();
    }

    public void setDataFile(File file) {
        this.dataFile = file;
    }

    protected void onSave() {
        save();
    }

    protected void save() {
        try {
            RngMeterSaveData saveData = new RngMeterSaveData();

            for (Map.Entry<String, RngMeterHUD.RngMeterData> entry : DungeonRngManager.INSTANCE.meters.entrySet()) {
                String floor = entry.getKey();
                RngMeterHUD.RngMeterData meterData = entry.getValue();

                if (meterData.score > 0 || (meterData.item != null && !meterData.item.isEmpty())) {
                    saveData.dungeonData.put(floor, new RngMeterSaveData.RngMeterUserData(
                            meterData.score,
                            meterData.item,
                            meterData.needed > 0 ? meterData.needed : null
                    ));
                }
            }

            for (Map.Entry<String, RngMeterHUD.RngMeterData> entry : SlayerRngManager.INSTANCE.meters.entrySet()) {
                String slayer = entry.getKey();
                RngMeterHUD.RngMeterData meterData = entry.getValue();

                if (meterData.score > 0 || (meterData.item != null && !meterData.item.isEmpty())) {
                    saveData.slayerData.put(slayer, new RngMeterSaveData.RngMeterUserData(
                            meterData.score,
                            meterData.item,
                            meterData.needed > 0 ? meterData.needed : null
                    ));
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            if (dataFile.exists()) {
                try (Reader reader = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
                    Map<String, Object> fullData = gson.fromJson(reader, Map.class);
                    if (fullData != null) {
                        Map<String, Object> rngMeterMap = new HashMap<>();
                        rngMeterMap.put("dungeonData", saveData.dungeonData);
                        rngMeterMap.put("slayerData", saveData.slayerData);
                        fullData.put("rngMeter", rngMeterMap);

                        dataFile.getParentFile().mkdirs();
                        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
                            gson.toJson(fullData, writer);
                        }
                        return;
                    }
                } catch (Exception e) {
                    Necron.LOGGER.warn("Failed to update existing data.json: {}", e.getMessage());
                }
            }

            Map<String, Object> newData = new HashMap<>();
            Map<String, Object> rngMeterMap = new HashMap<>();
            rngMeterMap.put("dungeonData", saveData.dungeonData);
            rngMeterMap.put("slayerData", saveData.slayerData);
            newData.put("rngMeter", rngMeterMap);

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
                gson.toJson(newData, writer);
            }
        } catch (IOException e) {
            Necron.LOGGER.error("Failed to save RNG meter data: {}", e.getMessage());
        }
    }

    public void load() {
        if (!dataFile.exists()) return;
        try (Reader reader = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
            Map<String, Object> fullData = gson.fromJson(reader, Map.class);
            if (fullData != null && fullData.containsKey("rngMeter")) {
                Map<String, Object> rngMeterObj = (Map<String, Object>) fullData.get("rngMeter");
                loadMeterData(rngMeterObj, "data", DungeonRngManager.INSTANCE.meters);
                loadMeterData(rngMeterObj, "slayerData", SlayerRngManager.INSTANCE.meters);
            }
        } catch (Exception e) {
            Necron.LOGGER.error("Failed to load RNG meter data: {}", e.getMessage());
        }
    }

    private void loadMeterData(Map<String, Object> rngMeterObj, String dataKey, Map<String, RngMeterHUD.RngMeterData> targetMeters) {
        if (rngMeterObj != null && rngMeterObj.containsKey(dataKey)) {
            Map<String, Map<String, Object>> dataObj = (Map<String, Map<String, Object>>) rngMeterObj.get(dataKey);

            for (Map.Entry<String, Map<String, Object>> entry : dataObj.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> userData = entry.getValue();

                RngMeterHUD.RngMeterData meterData = new RngMeterHUD.RngMeterData(key, "", 0, 0);

                if (userData.containsKey("score")) {
                    Object scoreObj = userData.get("score");
                    if (scoreObj instanceof Double) {
                        meterData.score = ((Double) scoreObj).intValue();
                    } else if (scoreObj instanceof Integer) {
                        meterData.score = (Integer) scoreObj;
                    }
                }

                if (userData.containsKey("item")) {
                    meterData.item = (String) userData.get("item");
                }

                if (userData.containsKey("needed") && userData.get("needed") != null) {
                    Object neededObj = userData.get("needed");
                    if (neededObj instanceof Double) {
                        meterData.needed = ((Double) neededObj).intValue();
                    } else if (neededObj instanceof Integer) {
                        meterData.needed = (Integer) neededObj;
                    }
                } else if (meterData.item != null && !meterData.item.isEmpty()
                        && rngMeterValues.containsKey(key)
                        && rngMeterValues.get(key).containsKey(meterData.item)) {
                    meterData.needed = rngMeterValues.get(key).get(meterData.item);
                } else {
                    meterData.needed = 0;
                }

                targetMeters.put(key, meterData);
            }
        }
    }
}