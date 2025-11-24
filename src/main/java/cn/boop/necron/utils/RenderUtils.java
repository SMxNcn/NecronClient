package cn.boop.necron.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.chromaSpeed;
import static cn.boop.necron.config.impl.ClientHUDOptionsImpl.colorOffset;

public class RenderUtils {
    public static void glColor(int hex) {
        float alpha = (float)(hex >> 24 & 0xFF) / 255.0f;
        float red = (float)(hex >> 16 & 0xFF) / 255.0f;
        float green = (float)(hex >> 8 & 0xFF) / 255.0f;
        float blue = (float)(hex & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawRoundedRect(float x0, float y0, float x1, float y1, float radius, int color) {
        if (x0 == x1 || y0 == y1) {
            return;
        }
        float f2 = (float)(color >> 24 & 0xFF) / 255.0f;
        float f3 = (float)(color >> 16 & 0xFF) / 255.0f;
        float f4 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f5 = (float)(color & 0xFF) / 255.0f;
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(f3, f4, f5, f2);
        GL11.glBegin(5);
        GL11.glVertex2f(x0 + radius, y0);
        GL11.glVertex2f(x0 + radius, y1);
        GL11.glVertex2f(x1 - radius, y0);
        GL11.glVertex2f(x1 - radius, y1);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(x0, y0 + radius);
        GL11.glVertex2f(x0 + radius, y0 + radius);
        GL11.glVertex2f(x0, y1 - radius);
        GL11.glVertex2f(x0 + radius, y1 - radius);
        GL11.glEnd();
        GL11.glBegin(5);
        GL11.glVertex2f(x1, y0 + radius);
        GL11.glVertex2f(x1 - radius, y0 + radius);
        GL11.glVertex2f(x1, y1 - radius);
        GL11.glVertex2f(x1 - radius, y1 - radius);
        GL11.glEnd();
        GL11.glBegin(6);
        float f6 = x1 - radius;
        float f7 = y0 + radius;
        GL11.glVertex2f(f6, f7);
        int j;
        for (j = 0; j <= 18; ++j) {
            float f8 = (float)j * 5.0f;
            GL11.glVertex2f((float)((double)f6 + (double)radius * Math.cos(Math.toRadians(f8))), (float)((double)f7 - (double)radius * Math.sin(Math.toRadians(f8))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x0 + radius;
        f7 = y0 + radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            float f9 = (float)j * 5.0f;
            GL11.glVertex2f((float)((double)f6 - (double)radius * Math.cos(Math.toRadians(f9))), (float)((double)f7 - (double)radius * Math.sin(Math.toRadians(f9))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x0 + radius;
        f7 = y1 - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            float f10 = (float)j * 5.0f;
            GL11.glVertex2f((float)((double)f6 - (double)radius * Math.cos(Math.toRadians(f10))), (float)((double)f7 + (double)radius * Math.sin(Math.toRadians(f10))));
        }
        GL11.glEnd();
        GL11.glBegin(6);
        f6 = x1 - radius;
        f7 = y1 - radius;
        GL11.glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            float f11 = (float)j * 5.0f;
            GL11.glVertex2f((float)((double)f6 + (double)radius * Math.cos(Math.toRadians(f11))), (float)((double)f7 + (double)radius * Math.sin(Math.toRadians(f11))));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glEnable(2884);
        GL11.glDisable(3042);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    public static void drawBorderedRect(float x, float y, float width, float height,
                                        float lineWidth, int borderColor) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(lineWidth);
        glColor(borderColor);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawBorderedRoundedRect(float x, float y, float width, float height,
                                               float radius, float borderWidth, int borderColor) {
        if (width <= 0 || height <= 0 || radius <= 0) return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(borderWidth);
        glColor(borderColor);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i <= 9; i++) {
            float angle = (float) (Math.PI * 1.5 + (Math.PI * i / 18));
            GL11.glVertex2f(
                    (float)(x + width - radius + Math.cos(angle) * radius),
                    (float)(y + radius + Math.sin(angle) * radius)
            );
        }

        for (int i = 0; i <= 9; i++) {
            float angle = (float) (Math.PI * 0.0 + (Math.PI * i / 18));
            GL11.glVertex2f(
                    (float)(x + width - radius + Math.cos(angle) * radius),
                    (float)(y + height - radius + Math.sin(angle) * radius)
            );
        }

        for (int i = 0; i <= 9; i++) {
            float angle = (float) (Math.PI * 0.5 + (Math.PI * i / 18));
            GL11.glVertex2f(
                    (float)(x + radius + Math.cos(angle) * radius),
                    (float)(y + height - radius + Math.sin(angle) * radius)
            );
        }

        for (int i = 0; i <= 9; i++) {
            float angle = (float) (Math.PI + (Math.PI * i / 18));
            GL11.glVertex2f(
                    (float)(x + radius + Math.cos(angle) * radius),
                    (float)(y + radius + Math.sin(angle) * radius)
            );
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawOutlinedBlockESP(double x, double y, double z, Color color, float lineWidth, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) return;

        double viewerX = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * partialTicks;
        double viewerY = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * partialTicks;
        double viewerZ = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glLineWidth(lineWidth);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;
        float a = (float) color.getAlpha() / 255;

        GL11.glColor4f(r, g, b, a);
        RenderUtils.drawOutlinedBoundingBox(new AxisAlignedBB(
                x - viewerX, y - viewerY, z - viewerZ,
                x + 1 - viewerX, y + 1 - viewerY, z + 1 - viewerZ
        ));
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB bb, Color color, float lineWidth, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) return;

        double viewerX = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * partialTicks;
        double viewerY = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * partialTicks;
        double viewerZ = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(lineWidth);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;
        float a = (float) color.getAlpha() / 255;

        GL11.glColor4f(r, g, b, a);

        AxisAlignedBB adjustedBB = new AxisAlignedBB(
                bb.minX - viewerX, bb.minY - viewerY, bb.minZ - viewerZ,
                bb.maxX - viewerX, bb.maxY - viewerY, bb.maxZ - viewerZ
        );

        drawOutlinedBoundingBox(adjustedBB);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void draw3DLine(double x, double y, double z, double x1, double y1, double z1, Color color, float lineWidth) {
        double viewerX = Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double viewerY = Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double viewerZ = Minecraft.getMinecraft().getRenderManager().viewerPosZ;
        x -= viewerX;
        x1 -= viewerX;
        y -= viewerY;
        y1 -= viewerY;
        z -= viewerZ;
        z1 -= viewerZ;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;
        float a = (float) color.getAlpha() / 255;

        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void draw3DText(String text, double x, double y, double z, Color color, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();

        if (viewer == null) return;

        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        float width = mc.fontRendererObj.getStringWidth(text) / 2.0f;
        float height = mc.fontRendererObj.FONT_HEIGHT / 2.0f;

        float size = (float) Minecraft.getMinecraft().thePlayer.getDistance(x, y, z) / 10.0f;
        if (size < 1.1f) size = 1.1f;
        float scale = (size * 2f) / 100.0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x - viewerX,
                y - viewerY,
                z - viewerZ
        );
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;
        float a = (float) color.getAlpha() / 255;

        GL11.glColor4f(r, g, b, a);

        GlStateManager.color(r, g, b, a);
        mc.fontRendererObj.drawString(text, -width, -height, color.getRGB(), true);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void drawCircleOnBlock(double x, double y, double z, Color color, float lineWidth, float radius, float partialTicks) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) return;

        double viewerX = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * partialTicks;
        double viewerY = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * partialTicks;
        double viewerZ = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GL11.glLineWidth(lineWidth);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;
        float a = (float) color.getAlpha() / 255;

        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        double centerX = x + 0.5 - viewerX;
        double centerY = y + 1.0 - viewerY;
        double centerZ = z + 0.5 - viewerZ;
        int segments = 32;

        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double circleX = centerX + radius * Math.cos(angle); // 半径为0.6
            double circleZ = centerZ + radius * Math.sin(angle);
            GL11.glVertex3d(circleX, centerY, circleZ);
        }
        GL11.glEnd();

        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static Color getChromaColor(Color start, Color end, int index, int speed, int offset) {
        double currentTime = System.nanoTime() / 1_000_000_000.0;

        double cOffset;
        if (offset == 0) cOffset = index * (colorOffset / 100f);
        else cOffset = index * (offset / 100f);

        double cSpeed;
        if (speed == 0) cSpeed = chromaSpeed / 10f;
        else cSpeed = speed / 10f;

        double phase = (currentTime * cSpeed + cOffset) * 2 * Math.PI;

        float progress = (float) (0.5 + 0.5 * Math.sin(phase));
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * progress);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * progress);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * progress);

        return new Color(r, g, b);
    }

    public static boolean isValidSHiColorPattern(String colorSection) {
        String cleanColor = colorSection.replace("§", "");
        return cleanColor.matches("[0-9a-fA-F]{6}([0-9a-fA-F]{2})?") ||
                cleanColor.matches("([0-9a-fA-F]{2})?[0-9a-fA-F]{6}");
    }
}
