package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class ChatBlockerOptionsImpl extends ModConfig {
    public ChatBlockerOptionsImpl() {
        super("Chat Blocker", "necron/chatblocker.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Useful chat blocking features")
    public static boolean chatBlocker = true;
    @Switch(name = "Whitelist", description = "Enable whitelist to bypass chat blocking")
    public static boolean whitelistEnabled = false;
}