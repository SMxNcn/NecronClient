package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class RerollProtectOptionsImpl extends ModConfig {
    public RerollProtectOptionsImpl () {
        super("Reroll Protector", "necron/reroll.json");
        initialize();
    }

    @Switch(name = "Enable", description = "Enable Reroll Protector")
    public static boolean reroll = true;
    @Switch(name = "Protect Reroll", description = "Prevent clicking Reroll button on RNG items")
    public static boolean rerollProtect = true;
    @Switch(name = "Send RNG to party", description = "Send your RNG to teammates")
    public static boolean sendToParty = true;
}
