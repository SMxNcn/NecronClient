package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.RenderUtils;
import cn.boop.necron.utils.RotationUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.List;

import static cn.boop.necron.config.impl.FarmingOptionsImpl.*;

public class CropNuker {
    private static BlockPos targetBlockPos = null;
    private static boolean pressed = false;

    //AutoPath
    private static int currentWaypointIndex = 0;
    private static boolean needsRotation = false;
    private static boolean needsDirectionChange = false;
    private static boolean continueMovingAfterWaypoint = false;
    private static long waypointReachedTime = 0;
    private static long actionStartTime = 0;
    private static String targetDirection = "forward";
    private static boolean atWaypoint = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (Necron.mc.thePlayer != null && Necron.mc.currentScreen == null) {
            if (cropNuker) {
                if (!pressed) {
                    FailSafe.resetPositionTracking();
                }
                handleAutoWalk();
                if (!atWaypoint && !RotationUtils.isRotating()) {
                    startNuker();
                } else {
                    stopMovement();
                }
                pressed = true;
            } else {
                stopNuker();
                pressed = false;
            }
        }

        updateTargetBlock();
    }

    @SubscribeEvent
    public void onRenderTick(RenderWorldLastEvent event) {
        if(targetBlockPos != null && cropNuker) {
            RenderUtils.drawOutlinedBlockESP(
                    targetBlockPos.getX(),
                    targetBlockPos.getY(),
                    targetBlockPos.getZ(),
                    new Color(162, 102, 232),
                    2, event.partialTicks);
        }
    }

    private static void startNuker() {
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindAttack.getKeyCode(), true);
    }

    private static void stopMovement() {
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindBack.getKeyCode(), false);

        if (!melonMode) {
            KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), false);
        }
    }

    private static void stopNuker() {
        if (!pressed) return;

        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindAttack.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindBack.getKeyCode(), false);
    }

    private static void setMovementKeys(String direction) {
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindBack.getKeyCode(), false);

        switch (direction.toLowerCase()) {
            case "forward":
                KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), true);
                break;
            case "backward":
                KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindBack.getKeyCode(), true);
                break;
            case "left":
                KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindLeft.getKeyCode(), true);
                if (melonMode) KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), true);
                break;
            case "right":
                KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindRight.getKeyCode(), true);
                if (melonMode) KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindForward.getKeyCode(), true);
                break;
        }
    }

    private void handleAutoWalk() {
        List<Waypoint> waypoints = Waypoint.getWaypoints();
        if (waypoints.isEmpty()) return;

        if (currentWaypointIndex >= waypoints.size()) { // 确保索引在有效范围内
            currentWaypointIndex = 0; // 重新开始
            FailSafe.resetPositionTracking();
        }

        Waypoint currentWaypoint = waypoints.get(currentWaypointIndex);
        double distanceToWaypoint = Necron.mc.thePlayer.getDistance( // 检查是否到达路径点
                currentWaypoint.getX() + 0.5,
                currentWaypoint.getY() + 0.5,
                currentWaypoint.getZ() + 0.5
        );

        if (continueMovingAfterWaypoint) {
            long time = System.currentTimeMillis() - waypointReachedTime;
            if (time > delayTime) { // 继续移动阶段结束，恢复正常路径点处理
                continueMovingAfterWaypoint = false;
                atWaypoint = true;
                stopNuker();
                handleWaypointActions(currentWaypoint);
                actionStartTime = System.currentTimeMillis();
            }
        } else if (distanceToWaypoint < 0.6) { // 如果足够接近路径点，则认为已到达
            if (!atWaypoint) {
                continueMovingAfterWaypoint = true;
                waypointReachedTime = System.currentTimeMillis();
            } else {
                handleWaypointProcessing();
            }
        } else {
            if (atWaypoint) atWaypoint = false;
            setMovementKeys(targetDirection);
        }
    }

    private void handleWaypointActions(Waypoint waypoint) {
        float targetRotation = RotationUtils.normalizeAngle(waypoint.getRotation()); // 设置目标旋转角度
        RotationUtils.smoothRotateTo(targetRotation, RotationUtils.pitch(), 8.0f);
        needsRotation = false;

        targetDirection = waypoint.getDirection(); // 设置目标方向
        needsDirectionChange = true;

        setMovementKeys(targetDirection);
    }

    private void handleWaypointProcessing() {
        if (RotationUtils.isRotating()) return; // 检查是否还在旋转

        if (needsDirectionChange) { // 处理方向变更
            setMovementKeys(targetDirection);
            needsDirectionChange = false;
        }

        if (!needsRotation && System.currentTimeMillis() - actionStartTime > 500L) { // 如果所有动作都已完成，继续到下一个路径点
            atWaypoint = false;
            currentWaypointIndex++;
            startNuker();
        }
    }

    public static void reset(FailSafe.ResetReason reason) {
        stopNuker();
        cropNuker = false;
        pressed = false;
        targetBlockPos = null;
        currentWaypointIndex = 0;
        needsRotation = false;
        needsDirectionChange = false;
        atWaypoint = false;
        Necron.LOGGER.warn("{} Disabled Crop Nuker.", reason.getMessage());
    }

    private void updateTargetBlock() {
        if (Necron.mc.objectMouseOver != null &&
                Necron.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            targetBlockPos = Necron.mc.objectMouseOver.getBlockPos();
        } else {
            targetBlockPos = null;
        }
    }

    public static void setIndex(int index) {
        currentWaypointIndex = index;
    }
}
