package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.events.WaypointEventHandler;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RotationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
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
    private static String currentFilename = null;
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
        currentFilename = filename;
        waypointCache = JsonUtils.loadWaypoints(Necron.WP_FILE_DIR + filename + ".json");
        routeCompleted = false;
        routerNotified = false;

        if (lastFilename != null && lastFilename.equals(filename)) {
            currentWaypointIndex = lastWaypointIndex;
            lastFilename = null;
        } else {
            currentWaypointIndex = 0;
        }

        if (waypointCache.isEmpty()) {
            routeCompleted = true;
            routerNotified = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        boolean currentLeftClick = Mouse.isButtonDown(0);

        if (router && LocationUtils.inSkyBlock ) {
            if (!lastLeftClick && currentLeftClick && Necron.mc.currentScreen == null) {
                if (!WaypointEventHandler.isEditingWaypoint) handleLeftClick();
            }
        }
        lastLeftClick = currentLeftClick;
    }

    private void handleLeftClick() {
        if (isProcessing || Necron.mc.thePlayer.inventory.getCurrentItem() == null) return;
        String itemID = Utils.getSkyBlockID(Necron.mc.thePlayer.inventory.getCurrentItem());
        if (Etherwarp.isEtherwarpItem(Necron.mc.thePlayer.inventory.getCurrentItem())){
            if (waypointCache.isEmpty()) {
                if (!routerNotified) {
                    Utils.modMessage("Waypoints file not loaded.");
                    routerNotified = true;
                    return;
                }
            }
            if (currentWaypointIndex >= waypointCache.size() || currentWaypointIndex < 0) {
                if (!routerNotified) {
                    if (isLoop) {
                        currentWaypointIndex = 0;
                        routeCompleted = false;
                    } else {
                        Utils.modMessage("Route completed.");
                        routeCompleted = true;
                        routerNotified = true;
                        waypointCache.clear();
                        lastWaypointIndex = -1;
                        currentWaypointIndex = -1;
                    }
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
                    if (devMsg) Utils.modMessage("Rotating.");
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Necron.LOGGER.error("EtherwarpRouter", e);
                }
                Etherwarp.useEtherwarp();
                if (devMsg) Utils.modMessage("Etherwarp.");

                try {
                    Thread.sleep(500);
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
        } else {
            if (isToolItem(itemID)) return;

            lastFilename = currentFilename;
            lastWaypointIndex = currentWaypointIndex;

            waypointCache.clear();
            currentWaypointIndex = -1;
            targetPosition = null;
            if (!routerNotified) {
                Utils.modMessage("Holding incorrect item.");
                Utils.modMessage("§cRELOAD§7 waypoint on the Waypoint GUI!");
                routerNotified = true;
            }
        }
    }

    private static boolean isToolItem(String itemID) {
        return "TITANIUM_DRILL_4".equals(itemID) ||
                "DIVAN_DRILL".equals(itemID) ||
                "JUJU_SHORTBOW".equals(itemID) ||
                "TERMINATOR".equals(itemID) ||
                "DIAMOND_PICKAXE".equals(itemID) ||
                "MITHRIL_PICKAXE".equals(itemID) ||
                "REFINED_MITHRIL_PICKAXE".equals(itemID);
    }

    private boolean hasReachedTarget(BlockPos target) {
        if (target == null || Necron.mc.thePlayer == null) return false;
        double distance = Necron.mc.thePlayer.getDistance(target.getX(), target.getY(), target.getZ());
        return distance <= 1.5;
    }
}
