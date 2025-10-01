package cn.boop.necron.module.impl.ctjs;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.HUD.RNGMeterHUD;
import cn.boop.necron.utils.LocationUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RngMeterManager {
    public static final RngMeterManager INSTANCE = new RngMeterManager();

    private final Map<String, RNGMeterHUD.RngMeterData> meters = new HashMap<>();
    private final Map<String, Map<String, Integer>> rngMeterValues = new HashMap<>();
    private File dataFile;
    private final Gson gson = new Gson();

    public RngMeterManager() {
        File dir = new File(Necron.mc.mcDataDir, "config/necron/data");
        if (!dir.exists()) dir.mkdirs();
        dataFile = new File(dir, "data.json");
        load();
    }

    public static class RngMeterSaveData {
        public Map<String, RngMeterUserData> data = new HashMap<>();

        public static class RngMeterUserData {
            public int score;
            public String item;
            public Integer needed;

            public RngMeterUserData(int score, String item, Integer needed) {
                this.score = score;
                this.item = item;
                this.needed = needed;
            }
        }
    }

    /** 检查并初始化某层数据 */
    public void checkFloorDataExists(String floor) {
        if (!meters.containsKey(floor)) {
            meters.put(floor, new RNGMeterHUD.RngMeterData(floor, "", 0, 0));
        } else {
            RNGMeterHUD.RngMeterData data = meters.get(floor);
            if (data.item != null && !data.item.isEmpty()
                    && data.needed <= 0
                    && rngMeterValues.containsKey(floor)
                    && rngMeterValues.get(floor).containsKey(data.item)) {
                data.needed = rngMeterValues.get(floor).get(data.item);
            }
        }
    }

    public void addScore(String floor, int score) {
        checkFloorDataExists(floor);
        meters.get(floor).score += score;
        save();
    }

    public void setScore(String floor, int score) {
        checkFloorDataExists(floor);
        meters.get(floor).score = score;
        save();
    }

    public void setItem(String floor, String item) {
        checkFloorDataExists(floor);
        RNGMeterHUD.RngMeterData data = meters.get(floor);
        data.item = item;
        if (item == null || item.isEmpty()) {
            data.needed = 0;
        } else if (rngMeterValues.containsKey(floor) && rngMeterValues.get(floor).containsKey(item)) {
            data.needed = rngMeterValues.get(floor).get(item);
        }
        save();
    }

    /** 目标分数数据加载（启动时加载一次即可） */
    public void loadValuesFile(Map<String, Map<String, Integer>> values) {
        rngMeterValues.clear();
        rngMeterValues.putAll(values);
    }

    public static Map<String, Map<String, Integer>> loadRngMeterValues() {
        Gson gson = new Gson();
        try (InputStream inputStream = RngMeterManager.class.getResourceAsStream("/RNGMeterValues.json")) {
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

    /**
     * 获取指定楼层的meter数据
     * @param floor 楼层标识
     * @return 对应的RngMeterData
     */
    public RNGMeterHUD.RngMeterData getMeterForFloor(String floor) {
        checkFloorDataExists(floor);
        return meters.get(floor);
    }

    /**
     * 获取当前楼层的meter数据（基于LocationUtils）
     * @return 当前楼层的RngMeterData
     */
    public RNGMeterHUD.RngMeterData getCurrentFloorMeter() {
        if (LocationUtils.floor != null) {
            String floor = LocationUtils.floor.name.replaceAll("[()]", "");
            return getMeterForFloor(floor);
        }
        return null;
    }

    /**
     * 获取当前楼层的进度百分比
     * @return 百分比值(0-100)
     */
    public double getCurrentFloorMeterPercentage() {
        if (LocationUtils.floor != null) {
            String floor = LocationUtils.floor.name.replaceAll("[()]", "");
            return getMeterPercentage(floor);
        }
        return 0.0;
    }

    /**
     * 获取当前楼层的进度条
     * @return 进度条字符串
     */
    public String getCurrentFloorMeterBar() {
        if (LocationUtils.floor != null) {
            String floor = LocationUtils.floor.name.replaceAll("[()]", "");
            return getMeterBar(floor);
        }
        return "";
    }

    /** 持久化保存RNG数据 */
    public void save() {
        try {
            RngMeterSaveData saveData = new RngMeterSaveData();
            for (Map.Entry<String, RNGMeterHUD.RngMeterData> entry : meters.entrySet()) {
                String floor = entry.getKey();
                RNGMeterHUD.RngMeterData meterData = entry.getValue();

                if (meterData.score > 0 || (meterData.item != null && !meterData.item.isEmpty())) {
                    saveData.data.put(floor, new RngMeterSaveData.RngMeterUserData(
                            meterData.score,
                            meterData.item,
                            meterData.needed > 0 ? meterData.needed : null
                    ));
                }
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            if (dataFile.exists()) {
                try (Reader reader = new FileReader(dataFile)) {
                    Map<String, Object> fullData = gson.fromJson(reader, Map.class);
                    if (fullData != null) {
                        Map<String, Object> rngMeterMap = new HashMap<>();
                        rngMeterMap.put("data", saveData.data);
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
            rngMeterMap.put("data", saveData.data);
            newData.put("rngMeter", rngMeterMap);

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
                gson.toJson(newData, writer);
            }
        } catch (IOException e) {
            Necron.LOGGER.error("Failed to save RNG meter data: {}", e.getMessage());
        }
    }

    /** 启动时加载RNG数据 */
    public void load() {
        if (!dataFile.exists()) return;
        try (Reader reader = new FileReader(dataFile)) {
            Map<String, Object> fullData = gson.fromJson(reader, Map.class);
            if (fullData != null && fullData.containsKey("rngMeter")) {
                Map<String, Object> rngMeterObj = (Map<String, Object>) fullData.get("rngMeter");
                if (rngMeterObj != null && rngMeterObj.containsKey("data")) {
                    Map<String, Map<String, Object>> dataObj = (Map<String, Map<String, Object>>) rngMeterObj.get("data");

                    for (Map.Entry<String, Map<String, Object>> entry : dataObj.entrySet()) {
                        String floor = entry.getKey();
                        Map<String, Object> userData = entry.getValue();

                        checkFloorDataExists(floor);
                        RNGMeterHUD.RngMeterData meterData = meters.get(floor);

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
                                && rngMeterValues.containsKey(floor)
                                && rngMeterValues.get(floor).containsKey(meterData.item)) {
                            meterData.needed = rngMeterValues.get(floor).get(meterData.item);
                        } else {
                            meterData.needed = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Necron.LOGGER.error("Failed to load RNG meter data: {}", e.getMessage());
        }
    }

    /**
     * 计算指定楼层的RNG进度百分比
     * @param floor 楼层标识
     * @return 百分比值(0-100)
     */
    public double getMeterPercentage(String floor) {
        checkFloorDataExists(floor);
        RNGMeterHUD.RngMeterData data = meters.get(floor);
        if (data == null || data.needed <= 0) return 0.0;
        double percentage = (double) data.score / data.needed * 100;
        return Math.min(percentage, 100.0);
    }

    /**
     * 生成指定楼层的进度条字符串
     * @param floor 楼层标识
     * @return 进度条字符串
     */
    public String getMeterBar(String floor) {
        checkFloorDataExists(floor);
        RNGMeterHUD.RngMeterData data = meters.get(floor);
        if (data == null) return "";
        return generateMeterBar(data.score, data.needed);
    }

    /**
     * 生成指定数据的进度条字符串
     * @param score 当前分数
     * @param needed 目标分数
     * @return 进度条字符串
     */
    public String generateMeterBar(int score, int needed) {
        int bars = 15;
        double percentage = needed <= 0 ? 0 : Math.min((double) score / needed, 1.0);
        int progress = (int) Math.floor(bars * percentage);

        return "§d" + String.format("%,d", score) + " §a§m§l" + repeat(" ", progress)
                + "§7§m§l" + repeat(" ", bars - progress) + "§r §d" + String.format("%,d", needed);
    }

    public String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(s);
        return sb.toString();
    }

    public void setDataFile(File file) {
        this.dataFile = file;
    }
}