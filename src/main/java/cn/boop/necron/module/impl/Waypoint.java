package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.config.impl.FarmingOptionsImpl;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.RenderUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static cn.boop.necron.config.impl.WaypointOptionsImpl.*;

public class Waypoint {
    private int id;
    private int x;
    private int y;
    private int z;
    private String direction = "forward";
    private float rotation = 0.0f;
    private static int waypointCounter = 1;

    public Waypoint(int id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Waypoint(int id, int x, int y, int z, String direction, float rotation) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction != null ? direction : "forward";
        this.rotation = rotation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getX() { return x; }
    public void setX(int xIn) { this.x = xIn; }
    public int getY() { return y; }
    public void setY(int yIn) { this.y = yIn; }
    public int getZ() { return z; }
    public void setZ(int zIn) { this.z = zIn; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction != null ? direction : "forward"; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }


    private static List<Waypoint> waypoints = new ArrayList<>();
    private static String currentFile = null;

    public static void loadWaypoints(String filename, boolean reload) {
        if (filename == null || filename.isEmpty()) return;
        currentFile = Necron.WP_FILE_DIR + filename + ".json";
        waypoints = JsonUtils.loadWaypoints(currentFile);

        if (waypoints.isEmpty()) {
            waypoints = new ArrayList<>();
            boolean created = JsonUtils.saveWaypoints(waypoints, currentFile);
            Utils.modMessage("Created new waypoint file. §8[" + filename + "]");
            if (!created) return;
        } else if (reload) {
            Utils.modMessage("Reloaded current waypoint file. §8[" + filename + "]");
        } else {
            Utils.modMessage("Loaded " + waypoints.size() + " waypoints. §8[" + filename + "]");
        }

        if (!waypoints.isEmpty()) {
            int maxId = waypoints.stream()
                    .mapToInt(Waypoint::getId)
                    .max()
                    .orElse(0);
            waypointCounter = maxId + 1;
        } else {
            waypointCounter = 1;
        }
    }

    public static void addWaypoint(BlockPos pos) {
        if (currentFile == null) {
            Utils.modMessage("Waypoints file not loaded.");
            return;
        }

        if (waypoints == null) waypoints = new ArrayList<>();

        Waypoint newWaypoint = new Waypoint(waypointCounter++, pos.getX(), pos.getY(), pos.getZ());
        waypoints.add(newWaypoint);
        JsonUtils.saveWaypoints(waypoints, currentFile);
        Utils.modMessage("Added BlockPos{x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ() + "} as waypoint #" + (waypointCounter - 1));

        onWaypointsChanged();
    }

    public static void removeWaypoint(BlockPos pos) {
        if (currentFile == null) {
            Utils.modMessage("Waypoints file not loaded.");
            return;
        }
        boolean removed = waypoints.removeIf(wp -> wp.x == pos.getX() && wp.y == pos.getY() && wp.z == pos.getZ());

        if (removed) {
            for (int i = 0; i < waypoints.size(); i++) {
                waypoints.get(i).setId(i + 1);
            }
            waypointCounter = waypoints.size() + 1;
            saveWaypoints();
            Utils.modMessage("Removed waypoint at BlockPos{x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ() + "}");
        } else {
            Utils.modMessage("No waypoint found at BlockPos{x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ() + "}");
        }
    }

    private static void saveWaypoints() {
        if (currentFile != null) {
            JsonUtils.saveWaypoints(waypoints, currentFile);
        }
    }

    public static void renderWaypoints(float partialTicks) {
        if (waypoints.isEmpty()) return;
        if (FarmingOptionsImpl.cropNuker) return;
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        if (viewer == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2F);

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint wp1 = waypoints.get(i);
            Waypoint wp2 = waypoints.get(i + 1);

            double x1 = wp1.x + 0.5;
            double y1 = wp1.y + 0.5;
            double z1 = wp1.z + 0.5;
            double x2 = wp2.x + 0.5;
            double y2 = wp2.y + 0.5;
            double z2 = wp2.z + 0.5;

            RenderUtils.draw3DLine(x1, y1, z1, x2, y2, z2, lineColor.toJavaColor(), 1.5f);
        }

        for (Waypoint wp : waypoints) {
            RenderUtils.drawOutlinedBlockESP(wp.x, wp.y, wp.z, boxColor.toJavaColor(), 2f, partialTicks);
            RenderUtils.draw3DText("#" + wp.id, wp.x + 0.5, wp.y + 0.5, wp.z + 0.5, textColor.toJavaColor(), partialTicks);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void onWaypointsChanged() {
        EtherwarpRouter.currentWaypointIndex = 0;
        if (waypoints.isEmpty()) {
            EtherwarpRouter.routeCompleted = true;
            EtherwarpRouter.routerNotified = true;
        } else {
            EtherwarpRouter.routeCompleted = false;
            EtherwarpRouter.routerNotified = false;
        }
    }

    public static String getCurrentFile() {
        return currentFile;
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }
}
