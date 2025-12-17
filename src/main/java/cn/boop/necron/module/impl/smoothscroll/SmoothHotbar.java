package cn.boop.necron.module.impl.smoothscroll;

import cn.boop.necron.Necron;
import cn.boop.necron.mixin.MinecraftAccessor;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;

import static cn.boop.necron.config.impl.ScrollingOptionsImpl.hotbarSmoothness;
import static cn.boop.necron.config.impl.ScrollingOptionsImpl.smoothHotbarSc;

public class SmoothHotbar {
    private final int rolloverOffset = 4;
    private float selectedPixelBuffer = 0;
    private boolean masked = false;
    private int lastSelectedSlot = 0;
    private int effectiveRollover = 0;
    public static final SmoothHotbar INSTANCE = new SmoothHotbar();

    public int calculateHotbarX(int originalX) {
        if (!smoothHotbarSc) return originalX;

        InventoryPlayer inv = Necron.mc.thePlayer.inventory;
        int currentSlot = inv.currentItem;

        if (currentSlot != lastSelectedSlot) {
            if (lastSelectedSlot == 8 && currentSlot == 0) {
                effectiveRollover -= 1;
            } else if (lastSelectedSlot == 0 && currentSlot == 8) {
                effectiveRollover += 1;
            } else {
                int actualPosition = currentSlot * 20;
                if (Math.abs(selectedPixelBuffer - actualPosition) > 160) {
                    selectedPixelBuffer = actualPosition;
                    effectiveRollover = 0;
                }
            }
            lastSelectedSlot = currentSlot;
        }

        int target = (inv.currentItem - effectiveRollover * 9) * 20 - effectiveRollover * rolloverOffset;
        float animationSpeed = Math.min(Math.max(0.01f, hotbarSmoothness * 0.1f), 0.9f);
        float delta = getLastFrameDuration();
        selectedPixelBuffer = (float) ((selectedPixelBuffer - target) * Math.pow(animationSpeed, delta) + target);

        float distanceToTarget = Math.abs(selectedPixelBuffer - target);
        if (distanceToTarget < 2f) {
            selectedPixelBuffer = currentSlot * 20;
            effectiveRollover = 0;
        }

        if (Math.round(selectedPixelBuffer) < -10 - rolloverOffset) {
            selectedPixelBuffer += 9 * 20 + rolloverOffset;
            effectiveRollover -= 1;
        } else if (Math.round(selectedPixelBuffer) > 20 * 9 - 10 + rolloverOffset) {
            selectedPixelBuffer -= 9 * 20 + rolloverOffset;
            effectiveRollover += 1;
        }

        int x = originalX - inv.currentItem * 20;
        x += Math.round(selectedPixelBuffer);
        return x;
    }

    public boolean needsRolloverHandling() {
        return smoothHotbarSc && (Math.round(selectedPixelBuffer) < 0 || Math.round(selectedPixelBuffer) > 20 * 8);
    }

    public boolean isRolloverNegative() {
        return Math.round(selectedPixelBuffer) < 0;
    }

    public int getRolloverXAdjustment() {
        return 9 * 20;
    }

    public int getRolloverOffset() {
        return rolloverOffset;
    }

    public boolean shouldNegateOffset() {
        return Math.round(selectedPixelBuffer) > 20 * 8;
    }

    public void enableScissor(ScaledResolution res) {
        int x = res.getScaledWidth() / 2 - 91;
        int y = res.getScaledHeight() - 22;
        int scale = res.getScaleFactor();

        int windowHeight = Necron.mc.displayHeight;
        int scissorY = windowHeight - (y + 22) * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, scissorY, 182 * scale, 22 * scale);
        masked = true;
    }

    public void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        masked = false;
    }

    public boolean isMasked() {
        return masked;
    }

    private static float getLastFrameDuration() {
        Timer timer = ((MinecraftAccessor) Necron.mc).getTimer();
        return timer.elapsedPartialTicks;
    }
}
