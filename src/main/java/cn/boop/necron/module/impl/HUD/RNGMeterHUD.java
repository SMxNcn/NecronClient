package cn.boop.necron.module.impl.HUD;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.ctjs.RngMeterManager;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RenderUtils;

import java.awt.*;

import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.RngBackground;

public class RNGMeterHUD extends BasicHud {
    public RNGMeterHUD() {
        super(true, 960, 60);
        EventManager.INSTANCE.register(this);
    }

    @Exclude public float width = 0f;
    @Exclude public float height = 41f;
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
        if (!LocationUtils.inDungeon) return;
        RngMeterData meter = RngMeterManager.INSTANCE.getCurrentFloorMeter();
        float currentY = (y / scale) + 4f;

        if (Necron.mc.thePlayer == null || meter == null) {
            Necron.mc.fontRendererObj.drawString("§cNo RNG Meter Data!", (int) (x + 4), (int) (y + 4), 0xFFFFFF, true);
            return;
        }

        if (RngBackground) {
            RenderUtils.drawRoundedRect(
                    (x - paddingX + 1f) / scale,
                    (y - paddingY + 3f) / scale,
                    (x + 3f + this.width) / scale,
                    (y + 2f + this.height) / scale,
                    3f,
                    new Color(0, 0, 0, 128).getRGB());
        }

        String floorColor = meter.meterType.startsWith("M") ? "§c" : "§a";
        double percentage = RngMeterManager.INSTANCE.getCurrentFloorMeterPercentage();
        String title = "§dRNG Meter §8- " + floorColor + meter.meterType + " §8- §d" + String.format("%.2f", percentage) + "%";
        Necron.mc.fontRendererObj.drawString(title, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
        updateDimensions(title, scale);
        currentY += 9.0f;

        if (meter.score > 0 && meter.needed <= 0) {
            String scoreLine = "§7Stored Score: §d" + String.format("%,d", meter.score);
            Necron.mc.fontRendererObj.drawString(scoreLine, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            updateDimensions(scoreLine, scale);
            currentY += 9.0f;
        } else if (meter.needed > 0) {
            String itemLine = "§7Item: " + meter.item;
            Necron.mc.fontRendererObj.drawString(itemLine, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            updateDimensions(itemLine, scale);
            currentY += 9.0f;

            String meterBar = RngMeterManager.INSTANCE.getCurrentFloorMeterBar();
            Necron.mc.fontRendererObj.drawString(meterBar, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            updateDimensions(meterBar, scale);
            currentY += 9.0f;
        } else {
            String errorText = "§cNo RNG Meter Data!";
            Necron.mc.fontRendererObj.drawString(errorText, (int)(x / scale), (int)currentY, Color.WHITE.getRGB());
            updateDimensions(errorText, scale);
        }

        this.height = (currentY - (y / scale)) * scale;
    }

    private void updateDimensions(String text, float scale) {
        float textWidth = Necron.mc.fontRendererObj.getStringWidth(text) * scale;
        if (textWidth > this.width) {
            this.width = textWidth;
        }
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
