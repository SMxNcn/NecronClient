package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cn.boop.necron.config.ModConfig;

public class ScrollingOptionsImpl extends ModConfig {
    public ScrollingOptionsImpl() {
        super("Smooth Scrolling", "necron/scroll.json");
        initialize();
    }

    @Switch(name = "Enable Hotbar scrolling")
    public static boolean smoothHotbarSc = true;
    @Number(name = "Scroll Duration", description = "", min = 1, max = 1000)
    public static int scrollDuration = 100;
    @Number(name = "Bounce Back Multiplier", description = "", min = 0, max = 1)
    public static float bounceBackMultiplier = 0.2f;
    @Slider(name = "Hotbar Smoothness", description = "Smoothness of scroll (Set 0 to disable)", min = 0, max = 10)
    public static int hotbarSmoothness = 6;

    public static int hotbarRollover = 1;
}
