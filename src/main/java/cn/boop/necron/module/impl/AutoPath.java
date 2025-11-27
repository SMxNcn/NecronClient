package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.pathfinder.TpNodeManager;
import cn.boop.necron.utils.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.boop.necron.config.impl.AutoPathOptionsImpl.*;

public class AutoPath {
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static BlockPos currentTarget = null;
    private static List<BlockPos> currentPath = null;
    private static int currentStep = 0;
    private static boolean isNavigating = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!autoPath) return;
        if (renderPath && currentPath != null && !currentPath.isEmpty()) {
            renderPath();
        }
    }

    public static void startNavigation(BlockPos target) {
        if (isNavigating) {
            stopNavigation();
        }

        currentTarget = target;
        isNavigating = true;
        currentStep = 0;

        new Thread(() -> {
            try {
                List<BlockPos> path = PathfinderUtils.findCompleteTeleportPath(
                        Necron.mc.thePlayer.getPosition(), target, 30
                );

                if (path == null || path.isEmpty()) {
                    Utils.modMessage("§c无法找到路径到目标位置");
                    stopNavigation();
                    return;
                }

                currentPath = path;
                executeNextStep();

            } catch (Exception e) {
                Utils.modMessage("§c路径计算失败: " + e.getMessage());
                stopNavigation();
            }
        }).start();
    }

    public static void stopNavigation() {
        isNavigating = false;
        currentTarget = null;
        currentPath = null;
        currentStep = 0;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private static void executeNextStep() {
        if (!isNavigating || currentPath == null) {
            stopNavigation();
            return;
        }

        if (currentStep >= currentPath.size()) {
            Utils.modMessage("§a已成功到达目标位置");
            stopNavigation();
            return;
        }

        BlockPos nextPos = currentPath.get(currentStep);
        BlockPos currentPos = Necron.mc.thePlayer.getPosition();

        if (currentPos.equals(currentTarget) ||
                (currentStep == currentPath.size() - 1 && currentPos.distanceSq(currentTarget) <= 4.0)) {
            Utils.modMessage("§a已到达目标位置");
            stopNavigation();
            return;
        }

        boolean isLastStep = (currentStep == currentPath.size() - 1);

        Vec3 targetVec = new Vec3(nextPos.getX() + 0.5, nextPos.getY() + 0.8, nextPos.getZ() + 0.5);
        RotationUtils.asyncAimAt(targetVec, 0.5f);

        scheduler.schedule(() -> {
            if (!isNavigating) return;

            BlockPos actualCurrentPos = Necron.mc.thePlayer.getPosition();

            if (TpNodeManager.isTeleportSafe(actualCurrentPos, nextPos) &&
                    Etherwarp.isEtherwarpItem(Necron.mc.thePlayer.inventory.getCurrentItem())) {
                PlayerUtils.rightClick();
                currentStep++;

                scheduler.schedule(() -> {
                    if (!isNavigating) return;

                    BlockPos newPos = Necron.mc.thePlayer.getPosition();

                    boolean shouldComplete =
                            newPos.equals(currentTarget) ||
                                    newPos.distanceSq(currentTarget) <= 4.0 ||
                                    (isLastStep && newPos.distanceSq(currentTarget) <= 9.0) || // 最后一步放宽条件
                                    currentStep >= currentPath.size();

                    if (shouldComplete) {
                        Utils.modMessage("§a当前位置: " + newPos + ", 目标: " + currentTarget);
                        stopNavigation();
                    } else {
                        if (isNavigating) {
                            executeNextStep();
                        }
                    }
                }, 300, TimeUnit.MILLISECONDS);

            } else {
                if (!TpNodeManager.isTeleportSafe(actualCurrentPos, nextPos)) {
                    Utils.modMessage("§c传送位置不安全，重新计算路径");
                } else {
                    Utils.modMessage("§c请手持传送物品");
                }
                recalculatePath();
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    private static void recalculatePath() {
        if (currentTarget == null) return;

        new Thread(() -> {
            List<BlockPos> newPath = PathfinderUtils.findCompleteTeleportPath(
                    Necron.mc.thePlayer.getPosition(), currentTarget, 30
            );

            if (newPath == null || newPath.isEmpty()) {
                Utils.modMessage("§c无法重新计算路径");
                stopNavigation();
            } else {
                currentPath = newPath;
                currentStep = 0;
                Utils.modMessage("§a路径已重新计算，继续传送");
                executeNextStep();
            }
        }).start();
    }

    private static void renderPath() {
        if (currentPath == null || currentPath.size() < 2) return;

        Color pathColor = isNavigating ? Color.GREEN : Color.CYAN;

        for (int i = 0; i < currentPath.size() - 1; i++) {
            BlockPos current = currentPath.get(i);
            BlockPos next = currentPath.get(i + 1);

            double x1 = current.getX() + 0.5;
            double y1 = current.getY() + 1.0;
            double z1 = current.getZ() + 0.5;

            double x2 = next.getX() + 0.5;
            double y2 = next.getY() + 1.0;
            double z2 = next.getZ() + 0.5;

            RenderUtils.draw3DLine(x1, y1, z1, x2, y2, z2, pathColor, 2.0f);

            if (renderNodes) {
                RenderUtils.draw3DText(String.valueOf(i + 1),
                        current.getX() + 0.5, current.getY() + 2.0, current.getZ() + 0.5,
                        Color.WHITE, 0);
            }
        }

        if (!currentPath.isEmpty()) {
            BlockPos target = currentPath.get(currentPath.size() - 1);
            RenderUtils.drawCircleOnBlock(target.getX(), target.getY(), target.getZ(),
                    Color.RED, 3.0f, 0.6f ,0);

            RenderUtils.draw3DText("目标",
                    target.getX() + 0.5, target.getY() + 2.5, target.getZ() + 0.5,
                    Color.RED, 0);
        }
    }
}