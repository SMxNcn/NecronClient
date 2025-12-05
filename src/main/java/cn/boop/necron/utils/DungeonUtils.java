package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.regex.*;

public class DungeonUtils {
    public static ArrayList<EntityPlayer> teammates = new ArrayList<>();
    public static Map<String, DungeonPlayer> dungeonPlayers = new HashMap<>();
    private static boolean hasTriggered = false;
    private int ticks = 0;

    public static class DungeonPlayer {
        private final String playerName;
        private DungeonClass playerClass;
        private int classLevel;

        public DungeonPlayer(String playerName) {
            this.playerName = playerName;
        }

        public DungeonClass getPlayerClass() { return playerClass; }
        public int getClassLevel() { return classLevel; }

        public void setPlayerClass(DungeonClass playerClass) { this.playerClass = playerClass; }
        public void setClassLevel(int classLevel) { this.classLevel = classLevel; }

        @Override
        public String toString() {
            return String.format("DungeonInfo{name='%s', class=%s, level=%d}",
                    playerName, playerClass, classLevel);
        }
    }

    public static void reset() {
        teammates.clear();
        dungeonPlayers.clear();
        hasTriggered = false;
    }

    public enum DungeonClass {
        Archer("Archer", "§c"),
        Berserk("Berserk", "§6"),
        Healer("Healer", "§d"),
        Mage("Mage", "§b"),
        Tank("Tank", "§2");

        private final String className;
        private final String color;

        DungeonClass(String className, String color) {
            this.className = className;
            this.color = color;
        }

        public String getClassName() {
            return className;
        }

        public String getColor() {
            return color;
        }

        public static DungeonClass fromName(String name) {
            for (DungeonClass clazz : values()) {
                if (clazz.className.equals(name)) {
                    return clazz;
                }
            }
            return null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!LocationUtils.inDungeon) return;

        List<String> tabList = getTabList();
        if (tabList != null) {
            if (teammates.isEmpty()) updateTeammates(tabList);
        }

        if (ticks % 20 == 0) {
            checkScoreboard();
            ticks = 0;
        }
        ticks++;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!LocationUtils.inDungeon) return;

        String message = StringUtils.stripControlCodes(event.message.getFormattedText()).trim();
        if ("Starting in 4 seconds.".equals(message)) reset();
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        reset();
    }

    private void checkScoreboard() {
        boolean check = ScoreboardUtils.scoreboardContains("Starting in:");
        if (check && !hasTriggered) {
            new Thread(() -> {
                try {
                    Thread.sleep(5500);
                    updateDungeonPlayers();
                } catch (InterruptedException e) {
                    Necron.LOGGER.error("Error in DungeonUtils.checkScoreboard: ", e);
                }
            }, "Dungeon-Data").start();
            hasTriggered = true;
        } else if (!check && hasTriggered) {
            hasTriggered = false;
        }
    }

    private void updateDungeonPlayers() {
        try {
            List<String> tabList = getTabList();
            if (tabList == null) return;

            parseTabListPlayers(tabList);
            showCurrentClassInfo();
        } catch (Exception e) {
            Necron.LOGGER.error("Error updating dungeon players: ", e);
        }
    }

    private void parseTabListPlayers(List<String> tabList) {
        dungeonPlayers.clear();

        Pattern playerPattern = Pattern.compile("^\\[(\\d+)]\\s+([^()]+?)\\s*\\(([A-Za-z]+)\\s+([0IVXL]+)\\)$");

        for (int i = 0; i < 5; i++) {
            String line = tabList.get(1 + i * 4);
            String cleanLine = StringUtils.stripControlCodes(line).trim();

            Matcher matcher = playerPattern.matcher(cleanLine);
            if (matcher.matches()) {
                String playerName = matcher.group(2);
                String originalName = matcher.group(2).trim();
                String className = matcher.group(3);
                String romanLevel = matcher.group(4);

                playerName = Utils.clearMcUsername(playerName);

                if (playerName.isEmpty()) continue;

                Necron.LOGGER.info("Parsed player: {} | Class: {} | Level: {}",
                        playerName, className, romanLevel);

                DungeonPlayer player = new DungeonPlayer(playerName);

                if (className != null && romanLevel != null) {
                    DungeonClass dungeonClass = DungeonClass.fromName(className);
                    int classLevel = Utils.romanToInt(romanLevel);

                    if (dungeonClass != null) {
                        player.setPlayerClass(dungeonClass);
                        player.setClassLevel(classLevel);
                        Necron.LOGGER.debug("Updated player class: {} -> {} {}",
                                playerName, dungeonClass.getClassName(), classLevel);

                        dungeonPlayers.put(playerName, player);
                    }
                }
            }
        }
    }

    private void showCurrentClassInfo() {
        String currentPlayerName = Necron.mc.thePlayer.getName();
        DungeonPlayer player = dungeonPlayers.get(currentPlayerName);

        if (player != null && player.getPlayerClass() != null) {
            DungeonClass playerClass = player.getPlayerClass();
            int classLevel = player.getClassLevel();
            Utils.modMessage("You are playing " + playerClass.getColor() + playerClass.getClassName() + " " + classLevel + "§7.");
        }
    }

    public static List<String> getTabList() {
        List<String> tabList = TabUtils.getTabList();
        if(tabList.size() < 18 || !tabList.get(0).contains("§r§b§lParty §r§f(")) return null;
        return tabList;
    }

    private static void updateTeammates(List<String> tabList) {
        teammates.clear();
        for(int i = 0; i < 4; i++) {
            if (1 + i * 4 >= tabList.size()) continue;

            String text = StringUtils.stripControlCodes(tabList.get(1 + i * 4)).trim();
            String[] parts = text.split(" ");
            if (parts.length < 2) continue;

            String username = Utils.clearMcUsername(text.split(" ")[1]);
            if(Objects.equals(username, "")) continue;

            for(EntityPlayer playerEntity : Necron.mc.theWorld.playerEntities) {
                if(playerEntity.getName().equals(username)) {
                    teammates.add(playerEntity);
                }
            }
        }
    }

    public static String getPlayerClassColor(String playerName) {
        DungeonPlayer player = dungeonPlayers.get(playerName);
        if (player != null && player.getPlayerClass() != null) {
            return player.getPlayerClass().getColor();
        }

        String cleanName = Utils.clearMcUsername(playerName);
        player = dungeonPlayers.get(cleanName);
        if (player != null && player.getPlayerClass() != null) {
            return player.getPlayerClass().getColor();
        }

        for (Map.Entry<String, DungeonPlayer> entry : dungeonPlayers.entrySet()) {
            if (cleanName.startsWith(entry.getKey()) && entry.getKey().length() >= 3) {
                player = entry.getValue();
                if (player != null && player.getPlayerClass() != null) {
                    return player.getPlayerClass().getColor();
                }
            }
        }

        return "§f";
    }
}
