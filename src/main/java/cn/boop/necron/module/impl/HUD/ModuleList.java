package cn.boop.necron.module.impl.HUD;

import cn.boop.necron.Necron;
import cn.boop.necron.module.ModuleManager;
import cn.boop.necron.utils.RenderUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.*;

public class ModuleList {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && moduleList) {
            List<String> activeModules = new ArrayList<>(ModuleManager.getActiveModules());
            activeModules.sort((a, b) -> {
                FontRenderer fontRenderer = Necron.mc.fontRendererObj;
                int widthA = fontRenderer.getStringWidth(a);
                int widthB = fontRenderer.getStringWidth(b);

                if (widthA != widthB) {
                    return Integer.compare(widthB, widthA);
                }
                return a.compareTo(b);
            });

            int screenWidth = Necron.mc.displayWidth / 2;
            int x = screenWidth - 5;
            int y = 5;

            drawModuleList(activeModules, x, y);
        }
    }

    private void drawModuleList(List<String> modules, int x, int y) {
        if (modules.isEmpty()) return;

        FontRenderer fontRenderer = Necron.mc.fontRendererObj;
        int maxModuleNameWidth = 0;

        for (String module : modules) {
            int width = fontRenderer.getStringWidth(module);
            if (width > maxModuleNameWidth) {
                maxModuleNameWidth = width;
            }
        }

        int backgroundPadding = 1;
        int verticalSpacing = 2;

        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);

            Color textChColor = getChromaColor(startColor.toJavaColor(), endColor.toJavaColor(), i, 0);
            int moduleNameWidth = fontRenderer.getStringWidth(module);
            int moduleX = x - maxModuleNameWidth + (maxModuleNameWidth - moduleNameWidth);
            int moduleY = y + i * (fontRenderer.FONT_HEIGHT + verticalSpacing + 1) + 1;

            RenderUtils.drawRoundedRect(
                    x - moduleNameWidth - backgroundPadding - 6,
                    moduleY - backgroundPadding - 1,
                    x + backgroundPadding,
                    moduleY + fontRenderer.FONT_HEIGHT + backgroundPadding,
                    2,
                    new Color(53, 53, 53, 115).getRGB()
            );

            fontRenderer.drawStringWithShadow(module, moduleX - 5, moduleY, textChColor.getRGB());

            int rectX = x + backgroundPadding - 3;
            float rectWidth = 1f;
            int rectHeight = fontRenderer.FONT_HEIGHT;
            RenderUtils.drawRoundedRect(
                    rectX,
                    moduleY - 1,
                    rectX + rectWidth + 0.5f,
                    moduleY + rectHeight,
                    1,
                    textChColor.getRGB()
            );
        }
    }

    public static Color getChromaColor(Color start, Color end, int index, int speed) {
        double currentTime = System.nanoTime() / 1_000_000_000.0;
        double offset = index * (colorOffset / 100f);
        double cSpeed;
        if (speed == 0)  cSpeed = chromaSpeed / 10f;
        else cSpeed = speed / 10f;

        double phase = (currentTime * cSpeed + offset) * 2 * Math.PI;

        float progress = (float) (0.5 + 0.5 * Math.sin(phase));
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);

        return new Color(r, g, b);
    }
}