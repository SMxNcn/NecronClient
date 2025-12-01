package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cn.boop.necron.Necron;
import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.ModConfig;
import cn.boop.necron.config.NotificationType;
import cn.boop.necron.gui.LoadingScreen;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NecronOptionsImpl extends ModConfig {
    public NecronOptionsImpl() {
        super("Necron Settings", "necron/main.json");
        initialize();
    }

    public static boolean customMainMenu = true;

    @Button(name = "Open ROLL result", text = "Open", subcategory = "Client")
    Runnable rollResult = () -> {
        try {
            Desktop.getDesktop().open(new File("./logs/roll_log.txt"));
        } catch (IllegalArgumentException | IOException e) {
            ClientNotification.sendNotification("Action", "Failed to open roll log file", NotificationType.WARN, 5000);
            Necron.LOGGER.error("Failed to open roll log file");
        }
    };
    @Button(name = "Open features' guide", text = "Open", subcategory = "Client")
    Runnable guide = () -> {
        try {
            Desktop.getDesktop().browse(new java.net.URI("https://gitee.com/mixturedg/necron-client/blob/master/FEATURES.md"));
        } catch (Exception e) {
            ClientNotification.sendNotification("Action", "Failed to open guide", NotificationType.WARN, 5000);
            Necron.LOGGER.error("Failed to open guide");
        }
    };

    @Number(name = "Switch interval (s)", description = "Background switching interval", min = 0, max = 20, subcategory = "Main Menu")
    public static int switchInterval = 10;
    @Number(name = "Fade duration (ms)", description = "Fade duration of background", min = 0, max = 1000, subcategory = "Main Menu")
    public static int fadeDuration = 750;
    @Button(name = "Open background file folder", text = "Open", subcategory = "Main Menu")
    Runnable openBgPath = () -> {
        try {
            Desktop.getDesktop().open(new File(Necron.BG_FILE_DIR));
        } catch (IllegalArgumentException | IOException e) {
            ClientNotification.sendNotification("Action", "Failed to open background folder", NotificationType.WARN, 5000);
            Necron.LOGGER.error("Failed to open background folder");
        }
    };

    @Switch(name = "Custom Loading Screen", subcategory = "Client")
    public static boolean customLoadingScreen = true;
    @Button(name = "Black Background", text = "Open", subcategory = "Client")
    Runnable splash = () -> {
        try {
            Desktop.getDesktop().open(LoadingScreen.CONFIG_FILE);
        } catch (IllegalArgumentException | IOException e) {
            ClientNotification.sendNotification("Action", "Failed to open splash file", NotificationType.WARN, 5000);
            Necron.LOGGER.error("Failed to open splash file");
        }
    };
    @Info(text = "背景更改将在游戏重启后生效", type = InfoType.WARNING, subcategory = "Client")
    public static boolean ignore;
}
