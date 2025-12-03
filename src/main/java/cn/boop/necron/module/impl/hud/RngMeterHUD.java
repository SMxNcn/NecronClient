package cn.boop.necron.module.impl.hud;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cn.boop.necron.Necron;
import cn.boop.necron.events.SlayerEventHandler;
import cn.boop.necron.module.impl.rng.DungeonRngManager;
import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.Slayer;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RenderUtils;

import java.awt.*;

import static cn.boop.necron.config.impl.GUIOptionsImpl.RngBackground;
import static cn.boop.necron.config.impl.GUIOptionsImpl.rngMeter;

public class RngMeterHUD extends BasicHud {
    public RngMeterHUD() {
        super(true, 960, 60);
        EventManager.INSTANCE.register(this);
    }

    @Exclude public float width = 0f;
    @Exclude public float height = 0f;
    @Exclude float paddingX = 4f, paddingY = 4f;

    public static class RngMeterData {
        public int score;
        public int needed;
        public String item;
        public String meterType; // Dungeon, Slayer, Experiment, etc.

        public RngMeterData(String meterType, String item, int score, int needed) {
            this.meterType = meterType;
            this.item = item;
            this.score = score;
            this.needed = needed;
        }
    }

    @Override
    public void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
        if (!rngMeter) return;

        RngMeterData meter = null;
        if (LocationUtils.inDungeon) {
            meter = DungeonRngManager.INSTANCE.getCurrentFloorMeter();
        } else if (LocationUtils.inSkyBlock) {
            Slayer currentSlayer = SlayerEventHandler.getCurrentSlayer();
            if (currentSlayer != null && currentSlayer != Slayer.Unknown) {
                meter = SlayerRngManager.INSTANCE.getCurrentSlayerMeter();
            }
        }

        if (Necron.mc.thePlayer == null || meter == null) return;

        calculateDimensions(meter, scale);

        if (RngBackground) {
            RenderUtils.drawRoundedRect(
                    (x - paddingX) / scale,
                    (y - paddingY + 3f) / scale,
                    (x + 3f + this.width) / scale,
                    (y + 3f + this.height) / scale,
                    3f,
                    new Color(0, 0, 0, 140).getRGB());
        }

        drawContent(x, y, scale, meter);
    }

    private void drawContent(float x, float y, float scale, RngMeterData meter) {
        float currentY = (y / scale) + 4f;
        String title;
        String floorColor = meter.meterType.startsWith("M") ? "§c" : "§a";

        if ("E".equals(meter.meterType)) {
            title = "§dRNG Meter §8- §aE";
        } else {
            double percentage;
            if (LocationUtils.inDungeon) {
                percentage = DungeonRngManager.INSTANCE.getCurrentFloorMeterPercentage();
            } else {
                percentage = SlayerRngManager.INSTANCE.getCurrentSlayerMeterPercentage();
            }
            title = "§dRNG Meter §8- " + floorColor + meter.meterType.replace(" Slayer", "") + " §8- §d" + String.format("%.2f", percentage) + "%";
        }

        Necron.mc.fontRendererObj.drawString(title, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
        currentY += 9.0f;

        if (meter.score > 0 && meter.needed <= 0) {
            String scoreLine = "§7Stored Score: §d" + String.format("%,d", meter.score);
            Necron.mc.fontRendererObj.drawString(scoreLine, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            currentY += 9.0f;
        } else if (meter.needed > 0) {
            String itemLine = "§7Item: " + meter.item;
            Necron.mc.fontRendererObj.drawString(itemLine, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            currentY += 9.0f;

            String meterBar;
            if (LocationUtils.inDungeon) {
                meterBar = DungeonRngManager.INSTANCE.getCurrentFloorMeterBar();
            } else {
                meterBar = SlayerRngManager.INSTANCE.getCurrentSlayerMeterBar();
            }
            Necron.mc.fontRendererObj.drawString(meterBar, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            currentY += 9.0f;
        } else {
            String errorText = "§cNo RNG Meter Data!";
            Necron.mc.fontRendererObj.drawString(errorText, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            currentY += 9.0f;
        }

        this.height = currentY - (y / scale) * scale;
    }

    private void calculateDimensions(RngMeterData meter, float scale) {
        float calculatedWidth = 0f;
        float calculatedHeight = 0f;

        String title;
        String floorColor = meter.meterType.startsWith("M") ? "§c" : "§a";
        double percentage;
        if (LocationUtils.inDungeon) {
            percentage = DungeonRngManager.INSTANCE.getCurrentFloorMeterPercentage();
        } else {
            percentage = SlayerRngManager.INSTANCE.getCurrentSlayerMeterPercentage();
        }
        if ("E".equals(meter.meterType)) {
            title = "§dRNG Meter §8- §aE";
        } else {
            title = "§dRNG Meter §8- " + floorColor + meter.meterType.replace(" Slayer", "") + " §8- §d" + String.format("%.2f", percentage) + "%";
        }

        calculatedWidth = Math.max(calculatedWidth, Necron.mc.fontRendererObj.getStringWidth(title) * scale);
        calculatedHeight += 9.0f;

        if (meter.score > 0 && meter.needed <= 0) {
            String scoreLine = "§7Stored Score: §d" + String.format("%,d", meter.score);
            calculatedWidth = Math.max(calculatedWidth, Necron.mc.fontRendererObj.getStringWidth(scoreLine) * scale);
            calculatedHeight += 9.0f;
        } else if (meter.needed > 0) {
            String itemLine = "§7Item: " + meter.item;
            calculatedWidth = Math.max(calculatedWidth, Necron.mc.fontRendererObj.getStringWidth(itemLine) * scale);
            String meterBar;
            if (LocationUtils.inDungeon) {
                meterBar = DungeonRngManager.INSTANCE.getCurrentFloorMeterBar();
            } else {
                meterBar = SlayerRngManager.INSTANCE.getCurrentSlayerMeterBar();
            }
            calculatedWidth = Math.max(calculatedWidth, Necron.mc.fontRendererObj.getStringWidth(meterBar) * scale);
            calculatedHeight += 18.0f;
        } else {
            String errorText = "§cNo RNG Meter Data!";
            calculatedWidth = Math.max(calculatedWidth, Necron.mc.fontRendererObj.getStringWidth(errorText) * scale);
            calculatedHeight += 9.0f;
        }

        this.width = calculatedWidth;
        this.height = calculatedHeight + 4f;
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return this.width * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return this.height * scale;
    }
}
