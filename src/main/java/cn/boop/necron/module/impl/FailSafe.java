package cn.boop.necron.module.impl;

import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.NotificationType;
import cn.boop.necron.config.ResetReason;
import cn.boop.necron.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.FarmingOptionsImpl.cropNuker;

public class FailSafe {
    public static boolean voidFalling = false;
    private static ItemStack lastRenderedItem = null;
    private static final Pattern VISIT_PATTERN = Pattern.compile("\\[SkyBlock] (?:\\[.*?] )?(.*?) is visiting Your Garden!");

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (cropNuker) {
            CropNuker.reset();
            Waypoint.unloadWaypoints();
            ClientNotification.sendNotification("Crop Nuker", ResetReason.WORLD_CHANGE.getMessage(), NotificationType.WARN, 6000);
        }
    }

    /*@SubscribeEvent
    public void onHandItemChange(RenderHandEvent event) {
        if (Necron.mc.thePlayer == null) return;
        ItemStack heldItem = Necron.mc.thePlayer.getHeldItem();

        if (!ItemStack.areItemStacksEqual(lastRenderedItem, heldItem)) {
            onHandItemChanged(lastRenderedItem, heldItem);
            lastRenderedItem = heldItem != null ? heldItem.copy() : null;
        }
    }*/

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
            } else if (visitMatcher.matches()) {
                String playerName = visitMatcher.group(1);
                if (!ChatBlocker.isPlayerWhitelisted(playerName)) {
                    CropNuker.reset();
                    ClientNotification.sendNotification("Crop Nuker", ResetReason.PLAYER_VISIT.getMessage(), NotificationType.WARN, 5000);
                }
            }
        }
    }

    private static boolean shouldReset(ItemStack oldItem, ItemStack newItem) {
        if ((oldItem == null) != (newItem == null)) return true;
        if (oldItem == null) return false;

        String oldId = Utils.getSkyBlockID(oldItem);
        String newId = Utils.getSkyBlockID(newItem);

        if (!oldId.equals(newId)) return true;

        return isFarmingTool(oldItem) != isFarmingTool(newItem);
    }

    public static boolean isFarmingTool(ItemStack item) {
        if (item == null) return false;

        return Utils.getSkyBlockID(item).contains("THEORETICAL_HOE_") ||
                Utils.getSkyBlockID(item).contains("_DICER_") ||
                Utils.getSkyBlockID(item).contains("COCO_CHOPPER") ||
                Utils.getSkyBlockID(item).contains("CACTUS_KNIFE") ||
                Utils.getSkyBlockID(item).contains("FUNGI_CUTTER") ||
                Utils.getSkyBlockID(item).contains("_GARDENING_");
    }

    /*private static void onHandItemChanged(ItemStack oldItem, ItemStack newItem) {
        if (cropNuker) {
            if (shouldReset(oldItem, newItem)) {
                //CropNuker.reset();
                ClientNotification.sendNotification("Crop Nuker",
                        (isFarmingTool(oldItem) || isFarmingTool(newItem)) ? ResetReason.ITEM_CHANGE.getMessage() : "You are not holding a farming tool!",
                        NotificationType.WARN, 5000
                );
            }
        }
    }*/

    public static void onPlayerTeleport(String calledModule) {
        if (cropNuker && !voidFalling) {
            CropNuker.reset();
            ClientNotification.sendNotification(calledModule, ResetReason.TELEPORT.getMessage(), NotificationType.WARN, 5000);
        }
    }

    public static void resetPositionTracking() {
        voidFalling = false;
        lastRenderedItem = null;
    }
}
