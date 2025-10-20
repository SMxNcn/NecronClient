package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.pathfinder.TpNodeManager;
import net.minecraft.util.BlockPos;

import java.util.*;

public class PathfinderUtils {
    private static class Node implements Comparable<Node> {
        public BlockPos position;
        public Node parent;
        public double gCost; // 从起点到当前节点的成本
        public double hCost; // 启发式成本
        public double fCost; // gCost + hCost
        public int teleportCount; // 传送次数

        public Node(BlockPos position, Node parent, double gCost, double hCost, int teleportCount) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
            this.teleportCount = teleportCount;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fCost, other.fCost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return position.equals(node.position);
        }

        @Override
        public int hashCode() {
            return position.hashCode();
        }
    }

    public static BlockPos findNextTeleportStep(BlockPos start, BlockPos target, float yaw, float pitch, int maxTeleports) {
        if (Necron.mc.theWorld == null) return null;

        if (canTeleportDirectly(start, target)) {
            return target;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<BlockPos, Node> openSetMap = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, target), 0);
        openSet.add(startNode);
        openSetMap.put(start, startNode);

        int nodesProcessed = 0;
        int maxNodes = 300;

        while (!openSet.isEmpty() && nodesProcessed < maxNodes) {
            nodesProcessed++;

            Node current = openSet.poll();
            openSetMap.remove(current.position);
            closedSet.add(current.position);

            // 如果接近目标且没有阻挡，返回
            if (heuristic(current.position, target) <= 4.0 &&
                    !TpNodeManager.hasObstructionBetween(current.position, target)) {
                return reconstructNextStep(current);
            }

            List<BlockPos> teleportNodes = TpNodeManager.getReachableTeleportNodes(
                    current.position, yaw, pitch
            );

            teleportNodes.sort((a, b) -> {
                boolean aObstructed = TpNodeManager.hasObstructionBetween(current.position, a);
                boolean bObstructed = TpNodeManager.hasObstructionBetween(current.position, b);

                if (aObstructed && !bObstructed) return 1;
                if (!aObstructed && bObstructed) return -1;

                // 都阻挡或都不阻挡时，按距离排序
                return Double.compare(heuristic(a, target), heuristic(b, target));
            });

            for (BlockPos neighbor : teleportNodes) {
                if (closedSet.contains(neighbor)) continue;

                double newGCost = current.gCost + getTeleportCost(current, neighbor);
                Node neighborNode = openSetMap.get(neighbor);

                if (neighborNode == null || newGCost < neighborNode.gCost) {
                    Node newNode = new Node(neighbor, current, newGCost,
                            heuristic(neighbor, target), current.teleportCount + 1);

                    if (neighborNode == null) {
                        openSet.add(newNode);
                        openSetMap.put(neighbor, newNode);
                    } else {
                        neighborNode.parent = current;
                        neighborNode.gCost = newGCost;
                        neighborNode.fCost = newGCost + neighborNode.hCost;
                        neighborNode.teleportCount = current.teleportCount + 1;

                        openSet.remove(neighborNode);
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        System.out.println("搜索结束，处理了 " + nodesProcessed + " 个节点");

        Node bestNode = null;
        for (Node node : openSet) {
            if (!TpNodeManager.hasObstructionBetween(start, node.position)) {
                if (bestNode == null || node.fCost < bestNode.fCost) {
                    bestNode = node;
                }
            }
        }

        if (bestNode != null) {
            return reconstructNextStep(bestNode);
        }

        return null;
    }


    public static List<BlockPos> findCompleteTeleportPath(BlockPos start, BlockPos target, int maxTeleports) {
        System.out.println("开始计算完整路径从 " + start + " 到 " + target + ", 距离: " + heuristic(start, target));

        if (canTeleportDirectly(start, target)) {
            List<BlockPos> directPath = new ArrayList<>();
            directPath.add(target);
            return directPath;
        }

        List<BlockPos> path = new ArrayList<>();
        BlockPos current = start;
        int attempts = 0;

        float yaw = getYawToTarget(start, target);
        float pitch = getPitchToTarget(start, target);

        while (attempts < Math.min(maxTeleports, 30) && current != null && !current.equals(target)) {
            System.out.println("第 " + (attempts + 1) + " 步, 当前位置: " + current);

            BlockPos nextStep = findNextTeleportStep(current, target, yaw, pitch, 2);
            if (nextStep == null) {
                break;
            }

            if (nextStep.equals(current)) {
                break;
            }

            path.add(nextStep);
            current = nextStep;
            attempts++;

            if (heuristic(current, target) <= 4.0) {
                break;
            }

            yaw = getYawToTarget(current, target);
            pitch = getPitchToTarget(current, target);
        }

        if (!path.isEmpty() && !path.get(path.size() - 1).equals(target)) {
            path.add(target);
        }

        return path;
    }

    private static BlockPos reconstructNextStep(Node node) {
        if (node == null || node.parent == null) return null;

        List<Node> path = new ArrayList<>();
        Node current = node;

        while (current != null) {
            path.add(current);
            current = current.parent;
        }

        Collections.reverse(path);

        for (int i = 1; i < path.size(); i++) {
            if (!path.get(i).position.equals(path.get(0).position)) {
                return path.get(i).position;
            }
        }

        return null;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private static double getTeleportCost(Node from, BlockPos to) {
        double distance = Math.sqrt(from.position.distanceSq(to));

        if (TpNodeManager.hasObstructionBetween(from.position, to)) {
            return distance + 30.0;
        }

        return distance + 2.0;
    }

    public static float getYawToTarget(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
    }

    public static float getPitchToTarget(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, horizontalDist));
    }

    public static boolean canTeleportDirectly(BlockPos from, BlockPos to) {
        double distance = Math.sqrt(from.distanceSq(to));
        if (distance > 11) return false;

        if (TpNodeManager.hasObstructionBetween(from, to)) {
            return false;
        }

        return TpNodeManager.isTeleportSafe(from, to);
    }
}
