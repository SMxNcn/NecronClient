package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
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

    @SubscribeEvent
    public void ClientTickEvent(TickEvent.ClientTickEvent event) {
        if (title) {
            try {
                if (customTitle) {
                    Display.setTitle(titleText);
                } else {
                    Display.setTitle("Minecraft 1.8.9" + " - Spongepowered Mixin v" + Necron.VERSION);
                }

                if (!iconsSet && icon) {
                    setWindowIcon();
                    iconsSet = true;
                }
            } catch (Exception e) {
                Necron.LOGGER.warn(e.getMessage());
            }
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
