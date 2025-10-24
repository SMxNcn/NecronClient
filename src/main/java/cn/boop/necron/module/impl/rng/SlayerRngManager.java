package cn.boop.necron.module.impl.rng;

import cn.boop.necron.Necron;
import cn.boop.necron.events.SlayerEventHandler;
import cn.boop.necron.module.impl.hud.RngMeterHUD;
import cn.boop.necron.module.impl.slayer.Slayer;

import java.io.File;

public class SlayerRngManager extends RngManager {
    public static final SlayerRngManager INSTANCE = new SlayerRngManager();

    public SlayerRngManager() {
        File dir = new File(Necron.mc.mcDataDir, "config/necron/data");
        if (!dir.exists()) dir.mkdirs();
        dataFile = new File(dir, "data.json");
    }

    public void checkSlayerDataExists(String slayerName) {
        checkDataExists(slayerName);
    }

    public void addScore(String slayerName, int score) {
        checkSlayerDataExists(slayerName);
        meters.get(slayerName).score += score;
        onSave();
    }

    public void setScore(String slayerName, int score) {
        super.setScore(slayerName, score);
    }

    public void setItem(String slayerName, String item) {
        super.setItem(slayerName, item);
        //super.setItem("Blaze Slayer", "§6High Class Archfiend Dice");
    }

    /**
     * 获取指定slayer的meter数据
     * @param slayerName 猎手类型
     * @return 对应的RngMeterData
     */
    public RngMeterHUD.RngMeterData getMeterForSlayer(String slayerName) {
        return getMeter(slayerName);
    }

    /**
     * 获取当前slayer的meter数据
     * @return 当前slayer的RngMeterData
     */
    public RngMeterHUD.RngMeterData getCurrentSlayerMeter() {
        Slayer currentSlayer = SlayerEventHandler.getCurrentSlayer();
        if (currentSlayer != null && currentSlayer != Slayer.Unknown) {
            return getMeterForSlayer(currentSlayer.getDisplayName());
        }
        return null;
    }

    /**
     * 获取当前slayer的进度百分比
     * @return 百分比值(0-100)
     */
    public double getCurrentSlayerMeterPercentage() {
        Slayer currentSlayer = SlayerEventHandler.getCurrentSlayer();
        if (currentSlayer != null && currentSlayer != Slayer.Unknown) {
            return getMeterPercentage(currentSlayer.getDisplayName());
        }
        return 0.0;
    }

    /**
     * 获取当前slayer的进度条
     * @return 进度条字符串
     */
    public String getCurrentSlayerMeterBar() {
        Slayer currentSlayer = SlayerEventHandler.getCurrentSlayer();
        if (currentSlayer != null && currentSlayer != Slayer.Unknown) {
            return getMeterBar(currentSlayer.getDisplayName());
        }
        return "";
    }
}