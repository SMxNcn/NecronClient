package cn.boop.necron.module.impl;

import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.NotificationType;
import cn.boop.necron.config.ResetReason;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.FarmingOptionsImpl.*;

public class FailSafe {
    public static boolean voidFalling = false;
    private static final Pattern VISIT_PATTERN = Pattern.compile("\\[SkyBlock] (?:\\[.*?] )?(.*?) is visiting Your Garden!");

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (cropNuker) {
            CropNuker.reset();
            Waypoint.unloadWaypoints();
            ClientNotification.sendNotification("Crop Nuker", ResetReason.WORLD_CHANGE.getMessage(), NotificationType.WARN, 6000);
            if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", ResetReason.WORLD_CHANGE.getMessage(), TrayIcon.MessageType.WARNING);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (cropNuker && event.type == 0) {
            String message = event.message.getUnformattedText();
            Matcher visitMatcher = VISIT_PATTERN.matcher(message);

            if (" ☠ You fell into the void.".equals(message)) {
                voidFalling = true;
            }

            if ("[ዞ]".equals(message)) {
                CropNuker.reset();
                ClientNotification.sendNotification("Crop Nuker", "ADMIN正在视奸你！！！", NotificationType.WARN, 10000);
                if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", "注意，ADMIN正在视奸你！！！", TrayIcon.MessageType.WARNING);
            } else if (visitMatcher.matches()) {
                String playerName = visitMatcher.group(1);
                if (!ChatBlocker.isPlayerWhitelisted(playerName)) {
                    CropNuker.reset();
                    if (cnAutoKick) Utils.chatMessage("/sbkick " + playerName);
                    ClientNotification.sendNotification("Crop Nuker", ResetReason.PLAYER_VISIT.getMessage(), NotificationType.WARN, 5000);
                    if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", ResetReason.PLAYER_VISIT.getMessage(), TrayIcon.MessageType.WARNING);
                }
            }
        }
    }

    public static void onPlayerTeleport(String calledModule) {
        if (cropNuker && !voidFalling) {
            CropNuker.reset();
            ClientNotification.sendNotification(calledModule, ResetReason.TELEPORT.getMessage(), NotificationType.WARN, 5000);
            if (cnSys) ClientNotification.sendSystemNotification(calledModule, ResetReason.TELEPORT.getMessage(), TrayIcon.MessageType.WARNING);
        }
    }

    public static void resetPositionTracking() {
        voidFalling = false;
    }
}
