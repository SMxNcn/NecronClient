package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cn.boop.necron.config.ModConfig;

public class TitleOptionsImpl extends ModConfig {
    public TitleOptionsImpl() {
        super("Title Manager", "necron/titlemanager.json");
        initialize();

        addDependency("titleText", "customTitle");
    }

    @Switch(name = "Change Title", description = "Use custom window title")
    public static boolean title = true;
    @Switch(name = "Change Icon", description = "Use Custom window icon")
    public static boolean icon = false;
    @Switch(name = "Use your title text", size = 2)
    public static boolean customTitle = false;
    @Text(name = "Title Text", placeholder = "Text here")
    public static String titleText = "";
}
