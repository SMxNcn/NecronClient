package cn.boop.necron.module.impl;

import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.NotificationType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static cn.boop.necron.config.impl.FarmingOptionsImpl.cropNuker;

public class FailSafe {
    private static boolean voidFalling = false;
    private static final double POSITION_THRESHOLD = 10.0;
    private static final double MOTION_THRESHOLD = 0.1;
    private static int motionCheckTicks = 0;
    private static final int MOTION_CHECK_DELAY = 100;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (cropNuker) {
            CropNuker.reset(ResetReason.WORLD_CHANGE);
            ClientNotification.sendNotification("Crop Nuker", ResetReason.WORLD_CHANGE.getMessage(), NotificationType.WARN, 6000);
        }
    }

//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.START) return;
//
//        if (Necron.mc.thePlayer != null && cropNuker) {
//            checkPosition();
//            checkMotion();
//        }
//    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (cropNuker && event.type == 0) {
            String message = event.message.getUnformattedText();
            if (" ☠ You fell into the void.".equals(message)) {
                voidFalling = true;
            }
        }
    }

//    private void checkMotion() {
//        if (cropNuker && !CropNuker.isAtWaypoint()) {
//            motionCheckTicks++;
//            if (motionCheckTicks >= MOTION_CHECK_DELAY) {
//                double motionX = Necron.mc.thePlayer.posX - lastPlayerX;
//                double motionZ = Necron.mc.thePlayer.posZ - lastPlayerZ;
//                double horizontalMotion = Math.sqrt(motionX * motionX + motionZ * motionZ);
//
//                if (horizontalMotion < MOTION_THRESHOLD) {
//                    CropNuker.reset(ResetReason.MOTION);
//                    ClientNotification.sendNotification("Crop Nuker", ResetReason.MOTION.getMessage(), ClientNotification.NotificationType.WARN, 5000);
//                }
//
//                motionCheckTicks = 0;
//            }
//        } else {
//            motionCheckTicks = 0;
//        }
//    }

    public static void onPlayerTeleport() {
        if (cropNuker && !voidFalling) {
            CropNuker.reset(ResetReason.TELEPORT);
            ClientNotification.sendNotification("Crop Nuker", ResetReason.TELEPORT.getMessage(), NotificationType.WARN, 5000);
        }
    }

    public static void resetPositionTracking() {
        voidFalling = false;
    }

    public enum ResetReason {
        WORLD_CHANGE("Detection server changed"),
        TELEPORT("Detection position changed");
        //MOTION("Detection incorrect movement");

        private final String message;

        ResetReason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
