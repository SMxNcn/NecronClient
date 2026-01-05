package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.events.WaypointEventHandler;
import cn.boop.necron.utils.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.boop.necron.config.impl.RouterOptionsImpl.*;

public class EtherwarpRouter {
    private boolean lastLeftClick = false;
    public static List<Waypoint> waypointCache = new ArrayList<>();
    public static int currentWaypointIndex = -1;
    private static String lastFilename = null;
    private static int lastWaypointIndex = -1;
    private boolean isProcessing = false;
    public static boolean routeCompleted = false;
    public static boolean routerNotified = false;
    private static BlockPos targetPosition = null;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "EtherwarpRouter-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public static void loadWaypoints(String filename) {
        waypointCache = JsonUtils.loadWaypoints(Necron.WP_FILE_DIR + filename + ".json");
        routeCompleted = routerNotified = false;

        if (lastFilename != null && lastFilename.equals(filename)) {
            currentWaypointIndex = lastWaypointIndex;
            lastFilename = null;
        } else {
            currentWaypointIndex = 0;
        }

        if (waypointCache.isEmpty()) {
            routeCompleted = routerNotified = true;
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event.button == 0 && event.buttonstate) {
            if (Etherwarp.isEtherwarpItem(Necron.mc.thePlayer.getHeldItem())) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!router || !LocationUtils.inSkyBlock || Necron.mc.currentScreen != null) return;
        boolean currentLeftClick = Mouse.isButtonDown(0);

        if (!lastLeftClick && currentLeftClick) {
            if (!WaypointEventHandler.isEditingWaypoint) handleLeftClick();
        }

        lastLeftClick = currentLeftClick;
    }

    private void handleLeftClick() {
        if (isProcessing || Necron.mc.thePlayer.inventory.getCurrentItem() == null) return;
        if (Etherwarp.isEtherwarpItem(Necron.mc.thePlayer.inventory.getCurrentItem())){
            if (waypointCache.isEmpty()) {
                if (!routerNotified) Utils.modMessage("Waypoints file not loaded.");
                routerNotified = true;
                return;
            }
            if (currentWaypointIndex >= waypointCache.size() || currentWaypointIndex < 0) {
                if (isLoop) {
                    currentWaypointIndex = 0;
                    routeCompleted = false;
                } else {
                    if (!routerNotified) Utils.modMessage("Route completed.");
                    if (alwaysSneak) PlayerUtils.setSneak(false);
                    routeCompleted = true;
                    routerNotified = true;
                    waypointCache.clear();
                    lastWaypointIndex = currentWaypointIndex = -1;
                }
                return;
            }

            Waypoint wp = waypointCache.get(currentWaypointIndex);

            BlockPos target = new BlockPos(wp.getX(), wp.getY(), wp.getZ());
            if (targetPosition == null || !targetPosition.equals(target)) {
                targetPosition = target;
            }

            Vec3 closestFaceCenter = RotationUtils.getClosestExposedFaceCenter(Necron.mc.theWorld, target, Necron.mc.thePlayer);
            if (closestFaceCenter == null) {
                Utils.modMessage("No exposed face found at #" + wp.getId());
                isProcessing = false;
                currentWaypointIndex++;
                targetPosition = null;
                return;
            }

            isProcessing = true;

            executor.submit(() -> {
                try {
                    RotationUtils.asyncAimAt(closestFaceCenter, 0.35f);
                    if (devMsg && !preAiming) Utils.modMessage("Rotating.");
                    int waitTime = preAiming  ? 20 : 200;
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Necron.LOGGER.error("EtherwarpRouter", e);
                }
                Etherwarp.useEtherwarp(alwaysSneak);
                if (devMsg) Utils.modMessage("Etherwarp.");

                try {
                    Thread.sleep(500);
                    handlePreAiming();
                } catch (Exception e) {
                    Necron.LOGGER.error("EtherwarpRouter", e);
                }

                if (hasReachedTarget(targetPosition)) {
                    currentWaypointIndex++;
                } else {
                    Utils.modMessage("Can't use etherwarp, check blocks on the path!");
                }

                isProcessing = false;
                targetPosition = null;
            }, "EtherwarpRouter");
        }
    }

    private static void handlePreAiming() {
        if (preAiming && !routeCompleted && !waypointCache.isEmpty()) {
            int nextWaypointIndex = currentWaypointIndex + 1;
            if (nextWaypointIndex < waypointCache.size()) {
                Waypoint nextWp = waypointCache.get(nextWaypointIndex);
                BlockPos nextTarget = new BlockPos(nextWp.getX(), nextWp.getY(), nextWp.getZ());
                Vec3 nextClosestFaceCenter = RotationUtils.getClosestExposedFaceCenter(Necron.mc.theWorld, nextTarget, Necron.mc.thePlayer);
                if (nextClosestFaceCenter != null) {
                    RotationUtils.asyncAimAt(nextClosestFaceCenter, 0.3f);
                    if (devMsg) Utils.modMessage("Pre-rotating.");
                }
            }
        }
    }

    private boolean hasReachedTarget(BlockPos target) {
        if (target == null || Necron.mc.thePlayer == null) return false;
        double distance = Necron.mc.thePlayer.getDistance(target.getX(), target.getY(), target.getZ());
        return distance <= 1.5;
    }
}
