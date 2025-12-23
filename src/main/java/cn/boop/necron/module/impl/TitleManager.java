package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static cn.boop.necron.config.impl.TitleOptionsImpl.*;

public class TitleManager {
    private static boolean iconsSet = false;
    private static long lastTipUpdate = 0;
    private static String currentTip = "";

    @SubscribeEvent
    public void ClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        try {
            setWindowTitle();

            if (!iconsSet && icon) {
                setWindowIcon();
                iconsSet = true;
            }
        } catch (Exception ignore) {}
    }

    private void setWindowTitle() {
        if (title && customType) {
            String title = "Minecraft 1.8.9";
            if (customPrefix) {
                title = prefixText;
            }
            if (showLocation) {
                String locationText = "";
                if (LocationUtils.inDungeon) locationText = "The Catacombs " + LocationUtils.floor.name.replaceAll("[()]", "");
                else if (LocationUtils.inSkyBlock && LocationUtils.currentIsland != null) locationText = LocationUtils.currentIsland.getDisplayName();

                if (!locationText.isEmpty()) title += " | " + locationText;
            }
            if (showPlayerName && Necron.mc.thePlayer != null) {
                title += " | " + Necron.mc.thePlayer.getName();
            }
            if (showTips) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTipUpdate > 10_000) {
                    currentTip = Utils.randomSelect(ChatCommands.tipList);
                    lastTipUpdate = currentTime;
                }
                title += " | "  + currentTip;
            }
            Display.setTitle(title);
        } else if (customTitle) {
            Display.setTitle(titleText);
        } else if (title) {
            Display.setTitle("Minecraft 1.8.9 - Spongepowered Mixin v" + Necron.VERSION);
        }
    }

    private void setWindowIcon() {
        try {
            BufferedImage icon16 = loadImage("assets/necron/gui/icon_16x16.png");
            BufferedImage icon32 = loadImage("assets/necron/gui/icon_32x32.png");

            ByteBuffer[] buffers = new ByteBuffer[] {
                    convertImageToBuffer(icon16),
                    convertImageToBuffer(icon32)
            };

            Display.setIcon(buffers);
        } catch (IOException e) {
            Necron.LOGGER.warn(e.getMessage());
        }
    }

    private BufferedImage loadImage(String path) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Icon not found: " + path);
        }
        return ImageIO.read(stream);
    }

    private ByteBuffer convertImageToBuffer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return buffer;
    }
}
