package cn.boop.necron.utils;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cn.boop.necron.Necron;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class LocationUtils {
    private static final HashMap<String, Island> ISLAND_MAPPING = createIslandMapping();
    private static final Pattern dragonPattern = Pattern.compile(".*- .* Dragon|.*No Alive Dragons");
    public static Island currentIsland = null;
    public static String currentZone = null;
    public static Floor floor = null;

    public static boolean inHypixel = false;
    public static boolean inSkyBlock = false;
    public static boolean inDungeon = false;
    public static boolean inBossRoom = false;
    private int ticks = 0;

    public enum Island {
        PRIVATE_ISLAND("Private Island"),
        HUB("Hub"),
        DARK_AUCTION("Dark Auction"),
        SPIDERS_DEN("Spider's Den"),
        CRIMSON_ISLE("Crimson Isle"),
        KUUDRA("Kuudra"),
        THE_END("The End"),
        GOLD_MINE("Gold Mine"),
        DEEP_CAVERNS("Deep Caverns"),
        DWARVEN_MINES("Dwarven Mines"),
        FARMING_ISLANDS("The Farming Islands"),
        DUNGEON_HUB("Dungeon Hub"),
        CRYSTAL_HOLLOWS("Crystal Hollows"),
        JERRYS_WORKSHOP("Jerry's Workshop"),
        BACKWATER_BAYOU("Backwater Bayou"),
        MINESHAFT("Mineshaft"),
        THE_RIFT("The Rift"),
        GARDEN("Garden");

        private final String tabName;

        Island(String tabName) {
            this.tabName = tabName;
        }
        public String getDisplayName() {
            return tabName;
        }
    }

    public enum Floor {
        ENTRANCE("(E)"),
        FLOOR_1("(F1)"),
        FLOOR_2("(F2)"),
        FLOOR_3("(F3)"),
        FLOOR_4("(F4)"),
        FLOOR_5("(F5)"),
        FLOOR_6("(F6)"),
        FLOOR_7("(F7)"),
        MASTER_1("(M1)"),
        MASTER_2("(M2)"),
        MASTER_3("(M3)"),
        MASTER_4("(M4)"),
        MASTER_5("(M5)"),
        MASTER_6("(M6)"),
        MASTER_7("(M7)");

        public final String name;

        Floor(String name) {
            this.name = name;
        }
    }

    public enum M7Phases {
        Unknown, P1, P2, P3, P4, P5
    }

    public enum P3Stages {
        Unknown(new BlockPos(-7, 160, -7), new BlockPos(7, 160, 7)),
        Tunnel(new BlockPos(39, 160, 54), new BlockPos(69, 112, 118)),
        S1(new BlockPos(89, 153, 51), new BlockPos(111, 105, 121)),
        S2(new BlockPos(89, 153, 121), new BlockPos(19, 105, 143)),
        S3(new BlockPos(19, 153, 121), new BlockPos(-3, 105, 51)),
        S4(new BlockPos(19, 153, 51), new BlockPos(89, 105, 29));

        private final BlockPos corner1;
        private final BlockPos corner2;

        P3Stages(BlockPos corner1, BlockPos corner2) {
            this.corner1 = corner1;
            this.corner2 = corner2;
        }

        public BlockPos getCorner1() { return corner1; }
        public BlockPos getCorner2() { return corner2; }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.world.isRemote) {
            ticks = 19;
            updateWorldStates();
            if (inSkyBlock) {
                updateCurrentIsland();
                updateFloor();
                updateZone();
                getBoss();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (ticks % 20 == 0) {
            updateWorldStates();
            if (inSkyBlock) {
                updateCurrentIsland();
                updateFloor();
                updateZone();
                getBoss();
            }
            ticks = 0;
        }
        ticks++;
    }

    private static HashMap<String, Island> createIslandMapping() {
        HashMap<String, Island> map = new HashMap<>();
        for (Island island : Island.values()) {
            map.put(island.getDisplayName(), island);
        }
        return map;
    }

    public static String getCurrentIslandName() {
        return currentIsland != null ? currentIsland.getDisplayName() : "N/A";
    }

    private void updateWorldStates() {
        if (Necron.mc.thePlayer != null && Necron.mc.theWorld != null) {
            ScoreObjective scoreboardObj = Necron.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            List<String> tabList = TabUtils.getTabList();
            String dungeonLine = null;
            if (tabList != null && tabList.size() > 41) dungeonLine = tabList.get(41);

            inHypixel = HypixelUtils.INSTANCE.isHypixel();
            inSkyBlock = scoreboardObj != null && Utils.removeFormatting(ScoreboardUtils.getScoreboardTitle()).contains("SKYBLOCK");
            inDungeon = inSkyBlock && dungeonLine != null && dungeonLine.startsWith("§r§b§lDungeon: §r");
        }
    }

    private void updateCurrentIsland() {
        currentIsland = null;
        if (!inSkyBlock) return;

        for (String tabEntry : TabUtils.getTabList()) {
            String cleaned = Utils.removeFormatting(tabEntry).trim();
            if (cleaned.startsWith("Area: ")) {
                String areaName = cleaned.substring(6).replaceAll("\\s+", " ");
                currentIsland = ISLAND_MAPPING.get(areaName);
                if (currentIsland != null) break;
            }
        }
    }

    private void updateZone() {
        List<String> scoreboard = ScoreboardUtils.getScoreboard();
        for(String line : scoreboard) {
            String cleanLine = ScoreboardUtils.cleanSB(line);
            if(cleanLine.matches("^ ⏣.*") || cleanLine.matches("^ ф.*")) {
                currentZone = cleanLine.substring(3);
                break;
            }
        }
    }

    public static void updateFloor() {
        List<String> sb = ScoreboardUtils.getScoreboard();
        String cataLine = ScoreboardUtils.getLineThatContains("The Catacombs");
        if (cataLine == null) return;

        for (Floor floorOption : Floor.values()) {
            if (cataLine.contains(floorOption.name)) {
                floor = floorOption;
                return;
            } else {
                for (String m7Line : sb) {
                    if (dragonPattern.matcher(m7Line).matches()) floor = Floor.MASTER_7;
                }
            }
        }
    }

    public static void getBoss() {
        if (floor == null) return;
        if (Necron.mc.thePlayer == null) return;

        double posX = Necron.mc.thePlayer.posX;
        double posZ = Necron.mc.thePlayer.posZ;

        if (inDungeon) {
            if (floor == Floor.FLOOR_7 || floor == Floor.MASTER_7) {
                inBossRoom = posX > -7 && posZ > -7;
            } else {
                inBossRoom = false;
            }
        }
    }

    public static M7Phases getM7Phase() {
        if (!inBossRoom) return M7Phases.Unknown;
        double posY = Necron.mc.thePlayer.posY;

        if (posY > 210) {
            return M7Phases.P1;
        } else if (posY > 155) {
            return M7Phases.P2;
        } else if (posY > 100) {
            return M7Phases.P3;
        } else if (posY > 45) {
            return M7Phases.P4;
        } else {
            return M7Phases.P5;
        }
    }

    public static P3Stages getP3Stage() {
        if (getM7Phase() != M7Phases.P3 || Necron.mc.thePlayer == null) return P3Stages.Unknown;

        double posX = Necron.mc.thePlayer.posX;
        double posY = Necron.mc.thePlayer.posY;
        double posZ = Necron.mc.thePlayer.posZ;
        BlockPos playerPos = new BlockPos(Math.floor(posX), Math.floor(posY), Math.floor(posZ));

        for (P3Stages stage : P3Stages.values()) {
            if (stage == P3Stages.Unknown) continue;

            if (Utils.isPlayerInArea(stage.getCorner1(), stage.getCorner2(), playerPos)) {
                return stage;
            }
        }

        return P3Stages.Unknown;
    }
}
