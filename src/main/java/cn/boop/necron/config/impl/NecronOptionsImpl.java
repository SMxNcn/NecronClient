package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.internal.assets.SVGs;
import cc.polyfrost.oneconfig.renderer.asset.Icon;
import cc.polyfrost.oneconfig.utils.Notifications;
import cn.boop.necron.Necron;
import cn.boop.necron.config.ModConfig;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class NecronOptionsImpl extends ModConfig {

    public NecronOptionsImpl () {
        super("Necron Settings", "necron/main.json");
        initialize();
    }

    public static boolean customMainMenu = true;

    @Button(name = "Open ROLL result", text = "Open", subcategory = "Client")
    Runnable rollResult = () -> {
        try {
            Desktop.getDesktop().open(new File("./logs/roll_log.txt"));
        } catch (IOException e) {
            Notifications.INSTANCE.send("Error", "Failed to open roll log file", new Icon(SVGs.X_CIRCLE_BOLD));
            Necron.LOGGER.error("roll result file");
        }
    };
    @Button(name = "Open features' guide", text = "Open", subcategory = "Client")
    Runnable guide = () -> {
        try {
            Desktop.getDesktop().browse(new java.net.URI("https://gitee.com/mixturedg/necron-client/blob/master/FEATURES.md"));
        } catch (Exception e) {
            Notifications.INSTANCE.send("Error", "Failed to open guide", new Icon(SVGs.X_CIRCLE_BOLD));
            Necron.LOGGER.error("Features");
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
        } catch (IOException e) {
            Notifications.INSTANCE.send("Error", "Failed to open background folder", new Icon(SVGs.X_CIRCLE_BOLD));
            Necron.LOGGER.error("Failed to open background folder");
        }
    };
}
