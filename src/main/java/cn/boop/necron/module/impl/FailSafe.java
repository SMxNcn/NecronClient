package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
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
    private static final Pattern PEST_PATTERN = Pattern.compile("^YUCK! (\\d) ൠ Pest have spawned in Plot - (\\d{1,2})!");

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (cropNuker) {
            if (cnReset) CropNuker.reset();
            else CropNuker.stopNotReset();
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
            Matcher pestMatcher = PEST_PATTERN.matcher(Utils.removeFormatting(message));

            if (" ☠ You fell into the void.".equals(message)) {
                voidFalling = true;
            }

            if (pestMatcher.matches()) {
                String pestCount = pestMatcher.group(1);
                String pestOnPlot = pestMatcher.group(2);
                if (cnPest) {
                    if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", pestCount + " Pest(s) spawned in Plot " + pestOnPlot + "!", TrayIcon.MessageType.INFO);
                }
            }

            if ("[ዞ]".equals(message)) {
                if (cnReset) CropNuker.reset();
                else CropNuker.stopNotReset();
                ClientNotification.sendNotification("Crop Nuker", "ADMIN正在视奸你！！！", NotificationType.WARN, 10000);
                if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", "注意，ADMIN正在视奸你！！！", TrayIcon.MessageType.WARNING);
            } else if (visitMatcher.matches()) {
                String playerName = visitMatcher.group(1);
                if (!ChatBlocker.isPlayerWhitelisted(playerName)) {
                    if (cnReset) CropNuker.reset();
                    else CropNuker.stopNotReset();
                    if (cnAutoKick) Utils.chatMessage("/sbkick " + playerName);
                    ClientNotification.sendNotification("Crop Nuker", ResetReason.PLAYER_VISIT.getMessage(), NotificationType.WARN, 5000);
                    if (cnSys) ClientNotification.sendSystemNotification("Crop Nuker", ResetReason.PLAYER_VISIT.getMessage(), TrayIcon.MessageType.WARNING);
                }
            }
        }
    }

    public static void onPlayerTeleport(String calledModule) {
        if (cropNuker && !voidFalling) {
            if (cnReset) CropNuker.reset();
            else CropNuker.stopNotReset();
            Utils.modMessage("Last location: " + Necron.mc.thePlayer.getPosition().toString());
            ClientNotification.sendNotification(calledModule, ResetReason.TELEPORT.getMessage(), NotificationType.WARN, 5000);
            if (cnSys) ClientNotification.sendSystemNotification(calledModule, ResetReason.TELEPORT.getMessage(), TrayIcon.MessageType.WARNING);
        }
    }

    public static void resetPositionTracking() {
        voidFalling = false;
    }
}
