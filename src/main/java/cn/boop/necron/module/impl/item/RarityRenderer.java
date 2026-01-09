package cn.boop.necron.module.impl.item;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class RarityRenderer {
    private static class RenderTask {
        final int x;
        final int y;
        final int color;

        RenderTask(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private static final List<RenderTask> renderTasks = new ArrayList<>();
    private static boolean isRenderingFrame = false;

    public static void beginFrame() {
        renderTasks.clear();
        isRenderingFrame = true;
    }

    public static void submitRarityBackground(int slotX, int slotY, EnumRarity rarity, int opacity) {
        if (!isRenderingFrame) return;

        int baseColor = rarity.getColor().getRGB();
        int alpha = (opacity << 24) & 0xFF000000;
        int color = alpha | (baseColor & 0xFFFFFF);

        renderTasks.add(new RenderTask(slotX, slotY, color));
    }

    public static void flushBatch() {
        if (renderTasks.isEmpty() || !isRenderingFrame) return;

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        for (RenderTask task : renderTasks) {
            Gui.drawRect(task.x, task.y, task.x + 16, task.y + 16, task.color);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        renderTasks.clear();
    }

    public static void endFrame() {
        renderTasks.clear();
        isRenderingFrame = false;
    }
}