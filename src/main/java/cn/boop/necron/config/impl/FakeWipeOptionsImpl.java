package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class FakeWipeOptionsImpl extends ModConfig {
    public FakeWipeOptionsImpl() {
        super("Fake Wipe", "necron/fakewipe.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Fake wipe")
    public static boolean fakeWipe = false;
    @Switch(name = "Can trigger ban gui", description = "Trigger ban gui")
    public static boolean triggerBan = false;
}
