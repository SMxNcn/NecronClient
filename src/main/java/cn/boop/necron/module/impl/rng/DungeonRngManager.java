package cn.boop.necron.module.impl.rng;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.hud.RngMeterHUD;
import cn.boop.necron.utils.LocationUtils;

import java.io.File;

public class DungeonRngManager extends RngManager {
    public static final DungeonRngManager INSTANCE = new DungeonRngManager();

    public DungeonRngManager() {
        File dir = new File(Necron.mc.mcDataDir, "config/necron/data");
        if (!dir.exists()) dir.mkdirs();
        dataFile = new File(dir, "data.json");
    }

    /** 检查并初始化某层数据 */
    public void checkFloorDataExists(String floor) {
        checkDataExists(floor);
    }

    public void addScore(String floor, int score) {
        checkFloorDataExists(floor);
        meters.get(floor).score += score;
        onSave();
    }

    public void setScore(String floor, int score) {
        super.setScore(floor, score);
    }

    public void setItem(String floor, String item) {
        super.setItem(floor, item);
    }

    /**
     * 获取指定楼层的meter数据
     * @param floor 楼层标识
     * @return 对应的RngMeterData
     */
    public RngMeterHUD.RngMeterData getMeterForFloor(String floor) {
        return getMeter(floor);
    }

    /**
     * 获取当前楼层的meter数据（基于LocationUtils）
     * @return 当前楼层的RngMeterData
     */
    public RngMeterHUD.RngMeterData getCurrentFloorMeter() {
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
}