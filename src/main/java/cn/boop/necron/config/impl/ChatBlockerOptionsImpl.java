package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.Necron;
import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.ModConfig;
import cn.boop.necron.config.NotificationType;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ChatBlockerOptionsImpl extends ModConfig {
    public ChatBlockerOptionsImpl() {
        super("Chat Blocker", "necron/chatblocker.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Useful chat blocking features")
    public static boolean chatBlocker = true;
    @Switch(name = "Whitelist", description = "Enable whitelist to bypass chat blocking")
    public static boolean whitelistEnabled = false;
    @Button(name = "Open whitelist file", text = "Open")
    Runnable whitelist = () -> {
        try {
            Desktop.getDesktop().open(new File("./config/necron/whitelist.json"));
        } catch (IllegalArgumentException | IOException e) {
            ClientNotification.sendNotification("Action", "Failed to open whitelist", NotificationType.WARN, 5000);
            Necron.LOGGER.error("Failed to open whitelist");
        }
    };
}