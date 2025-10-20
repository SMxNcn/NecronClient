package cn.boop.necron.module.impl.pathfinder;

import cn.boop.necron.Necron;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class TpNodeManager {
    private static final String NODES_STRING = "0:0:-12%5:0:-11%8:0:-8%11:0:-5%12:0:0%11:0:5%8:0:8%5:0:11%0:0:12%-5:0:11%-8:0:8%-11:0:5%-12:0:0%-11:0:-5%-8:0:-8%-5:0:-11%0:8:-8%6:8:-6%8:8:0%6:8:6%0:8:8%-6:8:6%-8:8:0%-6:8:-6%0:-8:-8%6:-8:-6%8:-8:0%6:-8:6%0:-8:8%-6:-8:6%-8:-8:0%-6:-8:-6";
    private static final List<BlockPos> PRECOMPUTED_NODES = parseNodes();

    private static List<BlockPos> parseNodes() {
        List<BlockPos> nodes = new ArrayList<>();
        String[] nodeArray = TpNodeManager.NODES_STRING.split("%");
        for (String node : nodeArray) {
            String[] coords = node.split(":");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            nodes.add(new BlockPos(x, y, z));
        }
        return nodes;
    }

    public static List<BlockPos> getTeleportNodes(BlockPos playerPos, float yaw, float pitch) {
        List<BlockPos> rotatedNodes = new ArrayList<>();

        for (BlockPos node : PRECOMPUTED_NODES) {
            BlockPos rotated = rotateNodeToView(node, yaw, pitch);
            rotatedNodes.add(playerPos.add(rotated));
        }

        return rotatedNodes;
    }

    private static BlockPos rotateNodeToView(BlockPos node, float yaw, float pitch) {
        double x = node.getX();
        double y = node.getY();
        double z = node.getZ();

        double yawRad = Math.toRadians(yaw + 90);
        double newX = x * Math.cos(yawRad) + z * Math.sin(yawRad);
        double newZ = -x * Math.sin(yawRad) + z * Math.cos(yawRad);

        double pitchRad = Math.toRadians(pitch);
        double horizontalDist = Math.sqrt(newX * newX + newZ * newZ);
        double newY = y * Math.cos(pitchRad) - horizontalDist * Math.sin(pitchRad);
        double newHorizontalDist = horizontalDist * Math.cos(pitchRad) + y * Math.sin(pitchRad);

        if (horizontalDist > 0) {
            double scale = newHorizontalDist / horizontalDist;
            newX *= scale;
            newZ *= scale;
        }

        return new BlockPos(
                Math.round(newX),
                Math.round(newY),
                Math.round(newZ)
        );
    }

    public static List<BlockPos> getReachableTeleportNodes(BlockPos playerPos, float yaw, float pitch) {
        List<BlockPos> allNodes = getTeleportNodes(playerPos, yaw, pitch);
        List<BlockPos> reachableNodes = new ArrayList<>();

        for (BlockPos node : allNodes) {
            if (isTeleportSafe(playerPos, node)) {
                reachableNodes.add(node);
            }
        }

        return reachableNodes;
    }

    public static boolean isTeleportSafe(BlockPos from, BlockPos to) {
        if (Necron.mc.theWorld == null) return false;

        if (!Necron.mc.theWorld.isBlockLoaded(to)) {
            return false;
        }

        double distance = Math.sqrt(from.distanceSq(to));
        if (distance > 14) return false;

        return hasLineOfSight(from, to) && !hasObstructionBetween(from, to);
    }

    private static boolean hasLineOfSight(BlockPos from, BlockPos to) {
        Vec3 start = new Vec3(from.getX() + 0.5, from.getY() + 1.6, from.getZ() + 0.5);
        Vec3 end = new Vec3(to.getX() + 0.5, to.getY() + 1.6, to.getZ() + 0.5);

        double distance = start.distanceTo(end);
        return distance <= 14;
    }

    public static boolean hasObstructionBetween(BlockPos from, BlockPos to) {
        if (Necron.mc.theWorld == null) return true;

        Vec3 start = new Vec3(from.getX() + 0.5, from.getY() + 1.6, from.getZ() + 0.5);
        Vec3 end = new Vec3(to.getX() + 0.5, to.getY() + 1.6, to.getZ() + 0.5);

        double distance = start.distanceTo(end);
        int steps = (int) (distance * 2);

        for (int i = 1; i < steps - 1; i++) {
            double ratio = (double) i / steps;
            double x = start.xCoord + (end.xCoord - start.xCoord) * ratio;
            double y = start.yCoord + (end.yCoord - start.yCoord) * ratio;
            double z = start.zCoord + (end.zCoord - start.zCoord) * ratio;

            BlockPos pos = new BlockPos(x, y, z);

            if (isSolidObstruction(pos)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSolidObstruction(BlockPos pos) {
        if (Necron.mc.theWorld == null) return true;
        if (pos.getY() < 0 || pos.getY() >= 256) return true;

        Block block = Necron.mc.theWorld.getBlockState(pos).getBlock();

        if (block instanceof BlockAir) return false;
        if (block instanceof BlockLiquid) return false;
        if (block instanceof BlockSnow) return false;
        if (block instanceof BlockTallGrass) return false;
        if (block instanceof BlockFlower) return false;
        if (block instanceof BlockFire) return false;
        if (block instanceof BlockVine) return false;
        if (block instanceof BlockWeb) return false;
        if (block instanceof BlockCarpet) return false;
        if (block instanceof BlockTorch) return false;

        return block.getMaterial().isSolid() ||
                block instanceof BlockFence ||
                block instanceof BlockFenceGate ||
                block instanceof BlockWall ||
                block instanceof BlockPane;
    }
}
