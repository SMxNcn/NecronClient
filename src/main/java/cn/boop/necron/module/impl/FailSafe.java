package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.config.ClientNotification;
import cn.boop.necron.config.NotificationType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.FarmingOptionsImpl.cropNuker;

public class FailSafe {
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    private static boolean posInit = false;
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
        } else if (Necron.mc.thePlayer != null) {
            posInit = false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (Necron.mc.thePlayer != null && cropNuker) {
            checkPosition();
//            checkMotion();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (cropNuker && event.type == 0) {
            String message = event.message.getUnformattedText();
            if (" ☠ You fell into the void.".equals(message)) {
                voidFalling = true;
                posInit = false;
            }
        }
    }

    private void checkPosition() {
        if (!posInit) {
            lastPlayerX = Necron.mc.thePlayer.posX;
            lastPlayerY = Necron.mc.thePlayer.posY;
            lastPlayerZ = Necron.mc.thePlayer.posZ;
            posInit = true;
            return;
        }

        double deltaX = Necron.mc.thePlayer.posX - lastPlayerX;
        double deltaY = Necron.mc.thePlayer.posY - lastPlayerY;
        double deltaZ = Necron.mc.thePlayer.posZ - lastPlayerZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        lastPlayerX = Necron.mc.thePlayer.posX;
        lastPlayerY = Necron.mc.thePlayer.posY;
        lastPlayerZ = Necron.mc.thePlayer.posZ;

        if (distance > POSITION_THRESHOLD && !voidFalling) {
            CropNuker.reset(ResetReason.TELEPORT);
            ClientNotification.sendNotification("Crop Nuker", ResetReason.TELEPORT.getMessage(), NotificationType.WARN, 5000);
        }

        voidFalling = false;
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

    public static void resetPositionTracking() {
        posInit = false;
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
