package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cn.boop.necron.config.ModConfig;

public class AutoLeapOptionsImpl extends ModConfig {
    public AutoLeapOptionsImpl() {
        super("Auto Leap", "necron/autoleap.json");
        initialize();
    }

    @Switch(name = "Enabled", description = "Auto leap to players based on predefined rules", subcategory = "Auto Leap")
    public static boolean autoLeap = false;
    @Switch(name = "Leap Notifier", description = "Send a party message when you leaping", subcategory = "Auto Leap")
    public static boolean leapNotifier = false;
    @Text(name = "Leap Text", placeholder = "Leap text here", subcategory = "Auto Leap")
    public static String leapText = "Leaped to %p!";
    @Info(text = "Player name placeholder: %p", type = InfoType.INFO, subcategory = "Auto Leap")
    public static boolean ignore;
}
