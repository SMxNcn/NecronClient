package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.block.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class PathfinderUtils {
    private static class Node implements Comparable<Node> {
        public int x, y, z;
        public BlockPos position;
        public Node parent;
        public double gCost; // 从起点到当前节点的成本
        public double hCost; // 从当前节点到终点的启发式成本
        public double fCost; // gCost + hCost
        public int fallDistance;

        public Node(int x, int y, int z) {
            this.position = new BlockPos(x, y, z);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Node(BlockPos position, Node parent, double gCost, double hCost) {
            this.position = position;
            this.x = position.getX();
            this.y = position.getY();
            this.z = position.getZ();
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
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

    // 定义可行走的方向（包括对角线）
    private static final int[][] DIRECTIONS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},  // 基本四个方向
            {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
            {1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}, // 对角线方向
            {1, 1, 1}, {1, 1, -1}, {-1, 1, 1}, {-1, 1, -1},
            {1, -1, 1}, {1, -1, -1}, {-1, -1, 1}, {-1, -1, -1},
            {0, 1, 0}, {0, -1, 0}  // 垂直方向
    };

    public static List<BlockPos> findPath(BlockPos start, BlockPos end) {
        if (Necron.mc.theWorld == null) return new ArrayList<>();

        // 开放列表（优先队列）
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        // 关闭列表（已访问节点）
        Set<Node> closedSet = new HashSet<>();
        // 用于快速查找开放列表中的节点
        Map<Node, Node> openSetMap = new HashMap<>();

        // 创建起始节点
        Node startNode = new Node(start, null, 0, 0);
        Node endNode = new Node(end, null, 0, 0);

        // 将起始节点加入开放列表
        openSet.add(startNode);
        openSetMap.put(startNode, startNode);

        while (!openSet.isEmpty()) {
            // 获取f值最小的节点
            Node currentNode = openSet.poll();
            openSetMap.remove(currentNode);

            // 添加到关闭列表
            closedSet.add(currentNode);

            // 如果到达终点
            if (currentNode.equals(endNode)) {
                return reconstructPath(currentNode);
            }

            // 按照到目标点的方向优先级排序方向数组
            int[][] sortedDirections = getSortedDirections(currentNode, endNode);

            // 检查所有邻居节点
            for (int[] dir : DIRECTIONS) {
                int newX = currentNode.x + dir[0];
                int newY = currentNode.y + dir[1];
                int newZ = currentNode.z + dir[2];

                Node neighbor = new Node(newX, newY, newZ);

                // 如果已经在关闭列表中，跳过
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // 检查是否可以行走
                if (!isWalkable(Necron.mc.theWorld, currentNode.x, currentNode.y, currentNode.z, newX, newY, newZ)) {
                    closedSet.add(neighbor);
                    continue;
                }

                // 计算到邻居节点的代价
                double movementCost = currentNode.gCost + getMovementCost(currentNode, neighbor);

                // 如果在开放列表中且新的路径更优，或者不在开放列表中
                if (movementCost < neighbor.gCost || !openSetMap.containsKey(neighbor)) {
                    neighbor.gCost = movementCost;
                    neighbor.hCost = getDistance(neighbor, endNode);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    neighbor.parent = currentNode;

                    if (!openSetMap.containsKey(neighbor)) {
                        openSet.add(neighbor);
                        openSetMap.put(neighbor, neighbor);
                    }
                }
            }
        }

        // 没有找到路径
        return new ArrayList<>();
    }

    // 重构路径
    private static List<BlockPos> reconstructPath(Node node) {
        List<BlockPos> path = new ArrayList<>();
        while (node != null) {
            path.add(new BlockPos(node.x, node.y, node.z));
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static int[][] getSortedDirections(Node current, Node target) {
        int dx = target.x - current.x;
        int dy = target.y - current.y;
        int dz = target.z - current.z;

        // 确定主要移动方向
        int primaryX = Integer.compare(dx, 0);
        int primaryY = Integer.compare(dy, 0);
        int primaryZ = Integer.compare(dz, 0);

        // 创建排序后的方向数组，优先考虑朝向目标的主要方向
        List<int[]> directionList = new ArrayList<>();
        List<int[]> priorityDirections = new ArrayList<>();
        List<int[]> otherDirections = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            // 如果方向与主要移动方向一致，给予高优先级
            if ((primaryX == 0 || Integer.compare(dir[0], 0) == primaryX || dir[0] == primaryX) &&
                    (primaryY == 0 || Integer.compare(dir[1], 0) == primaryY || dir[1] == primaryY) &&
                    (primaryZ == 0 || Integer.compare(dir[2], 0) == primaryZ || dir[2] == primaryZ)) {
                priorityDirections.add(dir);
            } else {
                otherDirections.add(dir);
            }
        }

        // 组合方向数组，优先方向在前
        directionList.addAll(priorityDirections);
        directionList.addAll(otherDirections);

        return directionList.toArray(new int[0][]);
    }

    // 检查能否走上一格高的台阶
    private static boolean canStepUp(BlockPos fromPos, BlockPos toPos) {
        if (toPos.getY() - fromPos.getY() != 1) {
            return false;
        }

        BlockPos fromBelow = fromPos.down();
        BlockPos toAbove = toPos.up();
        BlockPos toAbove2 = toPos.up(2);

        Block fromBelowBlock = Necron.mc.theWorld.getBlockState(fromBelow).getBlock();
        Block toCurrentBlock = Necron.mc.theWorld.getBlockState(toPos).getBlock();
        Block toAboveBlock = Necron.mc.theWorld.getBlockState(toAbove).getBlock();
        Block toAbove2Block = Necron.mc.theWorld.getBlockState(toAbove2).getBlock();

        // 检查是否为可攀爬方块
        if (isClimbableBlock(toCurrentBlock) || isClimbableBlock(toAboveBlock)) {
            return false;
        }

        // 检查目标位置是否可以通过
        if (isSolidBlock(toCurrentBlock) && !toCurrentBlock.isPassable(Necron.mc.theWorld, toPos)) {
            return false;
        }

        // 检查头部空间
        if (isSolidBlock(toAboveBlock) && !toAboveBlock.isPassable(Necron.mc.theWorld, toAbove)) {
            return false;
        }

        if (isSolidBlock(toAbove2Block) && !toAbove2Block.isPassable(Necron.mc.theWorld, toAbove2)) {
            return false;
        }

        // 检查是否能从当前站立位置跳到目标位置
        if (isBottomSlab(fromBelowBlock)) {
            // 如果当前站在下半砖上，检查目标位置是否过高
            return !isFullBlock(toCurrentBlock) && !isTopSlab(toCurrentBlock); // 避免从下半砖跳到完整方块或上半砖
        }

        return true;
    }

    // 检查能否下降
    private static boolean canStepDown(BlockPos fromPos, BlockPos toPos) {
        BlockPos toAbove = toPos.up();
        BlockPos toBelow = toPos.down();

        Block toCurrentBlock = Necron.mc.theWorld.getBlockState(toPos).getBlock();
        Block toAboveBlock = Necron.mc.theWorld.getBlockState(toAbove).getBlock();
        Block toBelowBlock = Necron.mc.theWorld.getBlockState(toBelow).getBlock();

        // 检查是否为可攀爬方块
        if (isClimbableBlock(toCurrentBlock) || isClimbableBlock(toAboveBlock)) {
            return false;
        }

        // 检查目标位置是否可以通过
        if (isSolidBlock(toCurrentBlock) && !toCurrentBlock.isPassable(Necron.mc.theWorld, toPos)) {
            return false;
        }

        // 检查头部空间
        if (isSolidBlock(toAboveBlock) && !toAboveBlock.isPassable(Necron.mc.theWorld, toAbove)) {
            return false;
        }

        // 检查下方是否可以站立
        if (!isSolidBlock(toBelowBlock)) {
            return canStandOnBlock(toBelowBlock);
        }

        return true;
    }

    private static boolean canJumpUp(BlockPos fromPos, BlockPos toPos) {
        int heightDiff = toPos.getY() - fromPos.getY();

        // 限制跳跃高度
        if (heightDiff > 1) {
            return false;
        }

        BlockPos fromBelow = fromPos.down();
        Block fromBelowBlock = Necron.mc.theWorld.getBlockState(fromBelow).getBlock();

        // 如果站在下半砖上，限制跳跃高度
        if (isBottomSlab(fromBelowBlock) && heightDiff > 1) {
            return false;
        }

        // 检查每个中间高度是否可通过
        for (int y = fromPos.getY() + 1; y <= toPos.getY(); y++) {
            BlockPos pos = new BlockPos(toPos.getX(), y, toPos.getZ());
            BlockPos posAbove = pos.up();

            Block block = Necron.mc.theWorld.getBlockState(pos).getBlock();
            Block blockAbove = Necron.mc.theWorld.getBlockState(posAbove).getBlock();

            if (isSolidBlock(block) && !block.isPassable(Necron.mc.theWorld, pos)) {
                return false;
            }

            if (isSolidBlock(blockAbove) && !blockAbove.isPassable(Necron.mc.theWorld, posAbove)) {
                return false;
            }
        }

        // 检查目标位置上方是否有空间
        BlockPos targetAbove = toPos.up();
        BlockPos targetAbove2 = toPos.up(2);

        Block targetAboveBlock = Necron.mc.theWorld.getBlockState(targetAbove).getBlock();
        Block targetAbove2Block = Necron.mc.theWorld.getBlockState(targetAbove2).getBlock();

        if (isSolidBlock(targetAboveBlock) && !targetAboveBlock.isPassable(Necron.mc.theWorld, targetAbove)) {
            return false;
        }

        return !isSolidBlock(targetAbove2Block) || targetAbove2Block.isPassable(Necron.mc.theWorld, targetAbove2);
    }

    // 检查能否下降
    private static boolean canFallDown(BlockPos fromPos, BlockPos toPos) {
        int heightDiff = fromPos.getY() - toPos.getY();

        // 限制下降高度（防止摔伤）
        if (heightDiff > 3) {
            return false;
        }

        // 检查目标位置是否可以站立
        BlockPos targetAbove = toPos.up();
        BlockPos targetBelow = toPos.down();

        Block targetBlock = Necron.mc.theWorld.getBlockState(toPos).getBlock();
        Block targetAboveBlock = Necron.mc.theWorld.getBlockState(targetAbove).getBlock();
        Block targetBelowBlock = Necron.mc.theWorld.getBlockState(targetBelow).getBlock();

        // 检查是否为可攀爬方块
        if (isClimbableBlock(targetBlock) || isClimbableBlock(targetAboveBlock)) {
            return false;
        }

        // 检查目标位置是否可以通过
        if (isSolidBlock(targetBlock) && !targetBlock.isPassable(Necron.mc.theWorld, toPos)) {
            return false;
        }

        // 检查头部空间
        if (isSolidBlock(targetAboveBlock) && !targetAboveBlock.isPassable(Necron.mc.theWorld, targetAbove)) {
            return false;
        }

        // 检查下方是否可以站立
        if (!isSolidBlock(targetBelowBlock)) {
            return canStandOnBlock(targetBelowBlock);
        }

        return true;
    }

    // 检查位置是否可行走
    private static boolean isWalkable(World world, int x, int y, int z, boolean skipCheck) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos posAbove = new BlockPos(x, y + 1, z);
        BlockPos posBelow = new BlockPos(x, y - 1, z);
        BlockPos posAbove2 = new BlockPos(x, y + 2, z);

        // 获取方块
        Block block = world.getBlockState(pos).getBlock();
        Block blockAbove = world.getBlockState(posAbove).getBlock();
        Block blockAbove2 = world.getBlockState(posAbove2).getBlock();
        Block blockBelow = world.getBlockState(posBelow).getBlock();

        // 检查是否为可攀爬方块
        if (isClimbableBlock(block) || isClimbableBlock(blockAbove)) {
            return false;
        }

        // 检查当前位置是否可以通过
        if (isSolidBlock(block) && !block.isPassable(world, pos)) {
            return false;
        }

        // 检查上方位置是否可以通过（玩家头部空间）
        if (isSolidBlock(blockAbove) && !blockAbove.isPassable(world, posAbove)) {
            return false;
        }

        // 检查上方第二个位置是否可以通过（跳跃时头部空间）
        if (isSolidBlock(blockAbove2) && !blockAbove2.isPassable(world, posAbove2)) {
            return false;
        }

        // 检查下方是否为固体方块（站立基础）
        if (!skipCheck && !isSolidBlock(blockBelow)) {
            // 特殊情况：检查是否可以站在半砖等特殊方块上
            if (!canStandOnBlock(blockBelow)) {
                return false;
            }
        }

        // 检查特殊方块类型
        return !isObstacleBlock(block) && !isObstacleBlock(blockAbove);
    }

    private static boolean isWalkable(World world, int x, int y, int z) {
        return isWalkable(world, x, y, z, false);
    }

    private static boolean isWalkable(World world, int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        BlockPos fromPos = new BlockPos(fromX, fromY, fromZ);
        BlockPos toPos = new BlockPos(toX, toY, toZ);

        if (!isWalkable(world, toX, toY, toZ)) {
            return false;
        }

        // 检查对角线移动时的碰撞（包括3D对角线）
        if (Math.abs(fromX - toX) == 1 && Math.abs(fromZ - toZ) == 1 && fromY == toY) {
            // 2D对角线移动（同一水平面），检查两个中间方块
            if (!isWalkable(world, fromX, fromY, toZ) || !isWalkable(world, toX, fromY, fromZ)) {
                return false;
            }
        } else if (Math.abs(fromX - toX) == 1 && Math.abs(fromZ - toZ) == 1 && Math.abs(fromY - toY) == 1) {
            // 3D对角线移动，检查三个中间方块
            if (!isWalkable(world, fromX, fromY, toZ) ||
                    !isWalkable(world, toX, fromY, fromZ) ||
                    !isWalkable(world, fromX, toY, fromZ)) {
                return false;
            }
        }

        // 检查垂直移动
        if (fromX == toX && fromZ == toZ) {
            // 垂直移动
            if (toY > fromY) {
                // 向上移动（跳跃）
                return canJumpUp(fromPos, toPos);
            } else {
                // 向下移动（下降）
                return canFallDown(fromPos, toPos);
            }
        }

        // 水平移动
        if (fromY == toY) {
            // 同一水平面移动
            return isWalkableOnSameLevel(fromPos, toPos);
        } else if (toY == fromY + 1) {
            // 上升一格（走上台阶/楼梯）
            return canStepUp(fromPos, toPos);
        } else if (toY == fromY - 1) {
            // 下降一格
            return canStepDown(fromPos, toPos);
        } else if (Math.abs(toY - fromY) > 1) {
            // 垂直距离过大
            return false;
        }

        return false;
    }

    // 检查能否在同一水平面行走
    private static boolean isWalkableOnSameLevel(BlockPos fromPos, BlockPos toPos) {
        BlockPos posAbove = toPos.up();
        BlockPos posBelow = toPos.down();

        Block block = Necron.mc.theWorld.getBlockState(toPos).getBlock();
        Block blockAbove = Necron.mc.theWorld.getBlockState(posAbove).getBlock();
        Block blockBelow = Necron.mc.theWorld.getBlockState(posBelow).getBlock();

        // 检查是否为可攀爬方块
        if (isClimbableBlock(block) || isClimbableBlock(blockAbove)) {
            return false;
        }

        // 检查当前位置是否可以通过
        if (isSolidBlock(block) && !block.isPassable(Necron.mc.theWorld, toPos)) {
            return false;
        }

        // 检查上方位置是否可以通过（玩家头部空间）
        if (isSolidBlock(blockAbove) && !blockAbove.isPassable(Necron.mc.theWorld, posAbove)) {
            return false;
        }

        // 检查下方是否为固体方块（站立基础）
        if (!isSolidBlock(blockBelow)) {
            if (!canStandOnBlock(blockBelow)) {
                return false;
            }
        }

        // 检查特殊方块类型
        return !isObstacleBlock(block) && !isObstacleBlock(blockAbove);
    }

    // 判断是否为实体方块
    private static boolean isSolidBlock(Block block) {
        return block.getMaterial().isSolid() && block.isFullCube();
    }

    // 判断是否为障碍方块
    private static boolean isObstacleBlock(Block block) {
        // 玻璃、栅栏、墙等障碍物
        return block instanceof BlockFence ||
                block instanceof BlockWall ||
                block instanceof BlockFenceGate || // 关闭的栅栏门
                block.getMaterial().isLiquid() || // 液体
                block.getMaterial().blocksMovement(); // 阻挡移动的方块
    }

    // 判断是否可以站在某个方块上
    private static boolean canStandOnBlock(Block block) {
        // 可以站立的方块：完整方块、半砖、楼梯等
        return block.isFullBlock() ||
                block.getMaterial().isSolid() ||
                block instanceof BlockSlab ||
                block instanceof BlockStairs ||
                block instanceof BlockCarpet;
    }

    // 判断是否为可攀爬方块（需要避免的方块）
    private static boolean isClimbableBlock(Block block) {
        return block instanceof BlockLadder ||
                block instanceof BlockVine ||
                block instanceof BlockWeb ||
                block.getUnlocalizedName().contains("ladder") ||
                block.getUnlocalizedName().contains("vine");
    }

    // 判断是否为完整方块
    private static boolean isFullBlock(Block block) {
        return block.isFullBlock() && block.getMaterial().isSolid();
    }

    // 判断是否为下半砖
    private static boolean isBottomSlab(Block block) {
        return block instanceof BlockSlab && !block.isFullBlock();
    }

    // 判断是否为上半砖
    private static boolean isTopSlab(Block block) {
        return block instanceof BlockSlab && !block.isFullBlock();
        // 注意：Minecraft中很难通过API直接判断是上半砖还是下半砖，这里简化处理
    }

    // 计算两个节点之间的距离（启发式函数）
    private static double getDistance(Node node1, Node node2) {
        int dx = Math.abs(node1.x - node2.x);
        int dy = Math.abs(node1.y - node2.y);
        int dz = Math.abs(node1.z - node2.z);

        // 使用对角线距离公式
        //return Math.sqrt(dx * dx + dy * dy + dz * dz);

        int min = Math.min(Math.min(dx, dy), dz);
        int max = Math.max(Math.max(dx, dy), dz);
        int mid = dx + dy + dz - min - max;

        // 八方向距离公式，更准确地估算3D空间中的最短距离
        return min * Math.sqrt(3) + (mid - min) * Math.sqrt(2) + (max - mid);
    }

    private static double getMovementCost(Node from, Node to) {
        int dx = Math.abs(from.x - to.x);
        int dy = from.y - to.y;
        int dz = Math.abs(from.z - to.z);

        // 基本移动成本
        double cost = Math.sqrt(dx * dx + dz * dz);

        // 垂直移动成本调整
        if (dy != 0) { // 有垂直移动
            if (dy > 0) { // 向下移动
                // 下降相对容易
                cost += dy * 0.8;
            } else { // 向上移动
                // 跳跃更困难
                int jumpHeight = Math.abs(dy);
                if (jumpHeight == 1) {
                    cost += 1.8; // 单格跳跃
                } else {
                    // 高跳跃成本很高（可能需要特殊处理）
                    cost += jumpHeight * 5.0;
                }
            }
        }

        return cost;
    }
}
