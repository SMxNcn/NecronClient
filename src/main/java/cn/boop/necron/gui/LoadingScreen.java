package cn.boop.necron.gui;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.GLUtils;
import cn.boop.necron.utils.RenderUtils;
import com.google.gson.Gson;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static cn.boop.necron.config.impl.NecronOptionsImpl.customLoadingScreen;

public class LoadingScreen {
    private static final String MOJANG_LOGO = "/assets/necron/gui/mojang.png";
    public static final File CONFIG_FILE = new File("config/necron/splash.json");
    private static LoadingScreenConfig config = new LoadingScreenConfig();
    private static int textureId = -1;
    private static boolean textureLoaded = false;

    public static int progress = 0;
    public static final int MAX_PROGRESS = 25;
    private static float animatedProgress = 0.0f;
    private static long lastUpdateTime = System.currentTimeMillis();

    private static boolean started = false;
    private static boolean finished = false;
    private static boolean mcLoaded = false;
    private static Drawable drawable;
    private static Thread thread;

    public static class LoadingScreenConfig {
        public boolean blackBg = false;
        public LoadingScreenConfig() {}
    }

    public static void setProgress(int progress) {
        LoadingScreen.progress = Math.max(0, Math.min(MAX_PROGRESS, progress));
    }

    public static void start() {
        loadConfig();
        if (started || !customLoadingScreen) return;
        started = true;

        try {
            drawable = new SharedDrawable(Display.getDrawable());
        } catch (LWJGLException e) {
            mcLoaded = true;
            return;
        }

        loadTextureDirectly();

        thread = new Thread(() -> {
            try {
                drawable.makeCurrent();

                while (!mcLoaded) {
                    int width = Display.getWidth();
                    int height = Display.getHeight();

                    GLUtils.resetGLState(width, height);
                    drawScreenContent(width, height);

                    Display.update();
                    Display.sync(60);
                }

                drawable.releaseContext();
                finished = true;
            } catch (Exception e) {
                Necron.LOGGER.error("Loading screen render thread error", e);
            }
        });
        thread.setName("Necron Loading Screen");
        thread.start();
    }

    public static void finish() {
        try {
            mcLoaded = true;

            if (thread != null) {
                thread.join();
            }

            finished = true;
        } catch (RuntimeException | InterruptedException e) {
            Necron.LOGGER.error("Error finishing loading screen", e);
        }
    }

    private static void loadTextureDirectly() {
        try {
            InputStream inputStream = LoadingScreen.class.getResourceAsStream(MOJANG_LOGO);
            if (inputStream == null) {
                Necron.LOGGER.error("Logo file not found: {}", MOJANG_LOGO);
                return;
            }

            BufferedImage image = ImageIO.read(inputStream);
            inputStream.close();

            if (image == null) return;

            textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

            ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = pixels[y * image.getWidth() + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            textureLoaded = true;

        } catch (Exception e) {
            Necron.LOGGER.error("Failed to load texture", e);
            textureLoaded = false;
        }
    }

    private static void drawScreenContent(int width, int height) {
        try {
            drawBackground(width, height);
            drawModernProgressBar(width, height);
            if (textureLoaded && textureId != -1) {
                drawTextureDirectly(width, height);
            }
        } catch (IllegalStateException ignore) {}
    }

    private static void drawBackground(int width, int height) {
        Color bgColor = config.blackBg ? new Color(0x000000) : new Color(0xEF323D);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(bgColor.getRed() / 255f, bgColor.getGreen() / 255f, bgColor.getBlue() / 255f, 1.0f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(width, 0);
        GL11.glVertex2f(width, height);
        GL11.glVertex2f(0, height);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void drawTextureDirectly(int width, int height) {
        try {
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            int baseLogoSize = Math.min(width, height) / 3;
            int logoSize = (int) (baseLogoSize * 3f);
            int x = (width - logoSize) / 2;
            int y = (height - logoSize) / 2 - 5;

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(x, y, logoSize, logoSize);

            GlStateManager.disableBlend();
        } catch (Exception e) {
            Necron.LOGGER.error("Error drawing texture", e);
        }
    }

    private static void drawModernProgressBar(int width, int height) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        float targetProgress = (float) progress / MAX_PROGRESS;
        animatedProgress += (targetProgress - animatedProgress) * Math.min(deltaTime * 2.0f, 1.0f);

        float scale = (float) width / 1280.0f;
        int barWidth = (int) (700 * scale);
        int barHeight = 15;
        int borderWidth = 2;
        int padding = 1;
        float cornerRadius = 2.0f;

        int x = (width - barWidth) / 2;
        int y = height - 135;
        int fillWidth = (int) ((barWidth - padding * 2) * animatedProgress);
        fillWidth = Math.min(fillWidth, barWidth - padding * 2);

        RenderUtils.drawBorderedRoundedRect(
                x - borderWidth,
                y - borderWidth,
                barWidth + borderWidth * 2,
                barHeight + borderWidth * 2,
                cornerRadius + 1,
                borderWidth,
                new Color(255, 255, 255, 255).getRGB()
        );

        if (fillWidth > 0) {
            RenderUtils.drawRoundedRect(
                    x + padding,
                    y + padding,
                    x + padding + fillWidth,
                    y + barHeight - padding,
                    cornerRadius - 1,
                    new Color(255, 255, 255,255).getRGB()
            );
        }
    }

    private static void drawTexturedModalRect(int x, int y, int width, int height) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(x, y + height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    private static void loadConfig() {
        try {
            if (!CONFIG_FILE.exists()) {
                saveConfig();
                return;
            }

            String content = new String(Files.readAllBytes(CONFIG_FILE.toPath()), StandardCharsets.UTF_8);
            config = new Gson().fromJson(content, LoadingScreenConfig.class);
        } catch (Exception e) {
            Necron.LOGGER.error("Failed to load loading screen config", e);
            config = new LoadingScreenConfig();
        }
    }

    private static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_FILE.getParentFile().toPath());
            String configContent = "{\n" +
                    "  \"//\": \"是否启用黑色背景（类似于高版本设置）\",\n" +
                    "  \"blackBg\": " + config.blackBg + "\n" +
                    "}";

            Files.write(CONFIG_FILE.toPath(), configContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Necron.LOGGER.error("Failed to save loading screen config", e);
        }
    }

    public static boolean isFinished() {
        return finished;
    }
}