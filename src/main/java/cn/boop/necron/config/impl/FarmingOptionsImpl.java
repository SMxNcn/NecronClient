package cn.boop.necron.config.impl;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.ModConfig;
import cn.boop.necron.config.NotificationType;
import cn.boop.necron.utils.Utils;

public class FarmingOptionsImpl extends ModConfig {
    public FarmingOptionsImpl() {
        super("Farming Helper", "necron/farming.json");
        initialize();

        registerKeyBind(cnKey, () -> {
            cropNuker = !cropNuker;
            String message = "Crop Nuker" + (cropNuker ? " Enabled" : " Disabled");
            NotificationType type = (cropNuker ? NotificationType.ENABLED : NotificationType.DISABLED);
            ClientNotification.sendNotification("Module", message, type, 2000);
        });
    }

    public static boolean cropNuker = true;

    @KeyBind(name = "Bind a key", description = "Legit crop nuker", subcategory = "Crop Nuker")
    public static OneKeyBind cnKey = new OneKeyBind();
    @Slider(name = "Max speed", description = "Max speed of Rancher's Boots", min = 0, max = 400, subcategory = "Crop Nuker")
    public static int maxSpeed = 200;
    @Button(name = "Set max speed", text = "Click", description = "Set max speed of Rancher's Boots", subcategory = "Crop Nuker")
    Runnable runnable = () -> Utils.chatMessage("/setmaxspeed " + maxSpeed);
    @Number(name = "Delay Time", description = "Edit the delay after reaching a waypoint", min = 0, max = 100, subcategory = "Crop Nuker")
    public static int delayTime = 50;
    @Checkbox(name = "Melon mode", description = "Max speed on advanced melon/pumpkin farm", subcategory = "Crop Nuker")
    public static boolean melonMode = false;
    @Switch(name = "Auto Kick", description = "Auto kick players without whitelisted", subcategory = "Crop Nuker")
    public static boolean cnAutoKick = false;
}
