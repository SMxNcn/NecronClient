package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SystemUtils {
    public static SystemUtils INSTANCE = new SystemUtils();
    private TrayIcon trayIcon;
    private boolean initialized = false;
    private long lastNotificationTime = 0;

    private SystemUtils() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;
        if (!SystemTray.isSupported()) return;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void initializeTray() {
        SwingUtilities.invokeLater(() -> {
            if (!initialized) init();
        });
    }

    private void init() {
        if (initialized || !Display.isCreated()) return;

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image icon = createIcon();
            if (icon == null) return;

            trayIcon = new TrayIcon(icon, "Necron v" + Necron.VERSION);
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://gitee.com/mixturedg/necron-client/"));
                        } catch (IOException | URISyntaxException ex) {
                            Necron.LOGGER.error(ex.getMessage());
                        }
                    }
                }
            });

            tray.add(trayIcon);
            initialized = true;

            Necron.LOGGER.info("TrayIcon initialized");

        } catch (Exception e) {
            Necron.LOGGER.error(e.getMessage());
        }
    }

    private Image createIcon() {
        try {
            String[] paths = {
                    "/assets/necron/gui/icon_32x32.png",
                    "/assets/necron/gui/icon_16x16.png"
            };

            for (String path : paths) {
                try {
                    java.io.InputStream stream = getClass().getResourceAsStream(path);
                    if (stream != null) {
                        BufferedImage img = ImageIO.read(stream);
                        stream.close();
                        if (img != null) {
                            Necron.LOGGER.info("TrayIcon loaded");
                            return img;
                        }
                    }
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            Necron.LOGGER.error(e.getMessage());
        }

        return null;
    }

    public void updateTooltip() {
        if (trayIcon == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        String tooltip = "Necron Client v" + Necron.VERSION;
        long currentTime = System.currentTimeMillis();

        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / maxMemory;
            String memoryInfo = String.format("Mem: %d/%d MB (%.1f%%)",
                    usedMemory / 1024 / 1024,
                    maxMemory / 1024 / 1024,
                    memoryUsage);

            if (mc.theWorld != null && mc.thePlayer != null) {
                String player = mc.thePlayer.getName();
                tooltip = memoryInfo + String.format(" | %s", player);
            }

            if (memoryUsage > 90 && currentTime - lastNotificationTime > 300_000) {
                sendNotification(
                        "Memory Warning",
                        String.format("当前可能存在内存溢出问题，建议重启游戏 (使用率: %.1f%%)", memoryUsage),
                        TrayIcon.MessageType.WARNING
                );
                lastNotificationTime = currentTime;
            }
        } catch (Exception ignore) {}

        trayIcon.setToolTip(tooltip);
    }

    public void sendNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon == null) return;

        trayIcon.displayMessage(title, message, type);
    }

    public void cleanup() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
            initialized = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (System.currentTimeMillis() % 1000 < 50) {
            updateTooltip();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}
