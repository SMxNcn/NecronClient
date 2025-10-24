package cn.boop.necron.events;

import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.Slayer;
import cn.boop.necron.module.impl.slayer.SlayerState;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.ScoreboardUtils;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.SlayerOptionsImpl.killTime;

public class SlayerEventHandler {
    private static final Map<LocationUtils.Island, Map<String, Slayer>> SLAYER_MAPPING = createSlayerMapping();

    private static long startTime = 0L;
    private static boolean inCombat = false;
    private static Slayer currentSlayer;
    private static SlayerState currentSlayerState;
    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!LocationUtils.inSkyBlock) return;
        updateSlayerState();
        if (ticks % 20 == 0) {
            updateSlayer();
            ticks = 0;
        }
        ticks++;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0 || !LocationUtils.inSkyBlock) return;
        String cleanMessage = Utils.removeFormatting(event.message.getFormattedText());

        Pattern SLAYER_DROP_PATTERN = Pattern.compile("^[A-Z ]+DROP!? \\(([^)]+)\\)");
        Matcher dropMatcher = SLAYER_DROP_PATTERN.matcher(cleanMessage);

        if (dropMatcher.find()) {
            System.out.println("Drop Pattern matched!");
            String item = dropMatcher.group(1);

            Slayer slayer = SlayerEventHandler.getCurrentSlayer();
            if (slayer != Slayer.Unknown) {
                SlayerRngManager.INSTANCE.setScore(slayer.getDisplayName(), 0);
                int score = SlayerRngManager.INSTANCE.getScore(slayer.getDisplayName());
                double percentage = SlayerRngManager.INSTANCE.getMeterPercentage(slayer.getDisplayName());
                Utils.modMessage("§dRng Item §7reset! (§6" + score + " §bScore, §6" + String.format("%.2f", percentage) + "§b%§7)");
            }
        }


    }

    private static Map<LocationUtils.Island, Map<String, Slayer>> createSlayerMapping() {
        Map<LocationUtils.Island, Map<String, Slayer>> mapping = new HashMap<>();

        for (LocationUtils.Island island : LocationUtils.Island.values()) {
            Map<String, Slayer> islandMapping = new HashMap<>();

            switch (island) {
                case HUB:
                    islandMapping.put("Ruins", Slayer.Sven);
                    islandMapping.put("Graveyard", Slayer.Revenant);
                    islandMapping.put("Coal Mine", Slayer.Revenant);
                    break;
                case SPIDERS_DEN:
                    islandMapping.put("Arachne's Burrow", Slayer.Tarantula);
                    break;
                case CRIMSON_ISLE:
                    islandMapping.put("Stronghold", Slayer.Inferno);
                    islandMapping.put("Smoldering Tomb", Slayer.Inferno);
                    break;
                case THE_END:
                    islandMapping.put("Zealot Bruiser Hideout", Slayer.Voidgloom);
                    islandMapping.put("Void Sepulture", Slayer.Voidgloom);
                    break;
                case THE_RIFT:
                    islandMapping.put("Stillgore Château", Slayer.Riftstalker);
                    break;
                default:
                    break;
            }

            mapping.put(island, islandMapping);
        }

        return mapping;
    }

    private static void updateSlayer() {
        if (LocationUtils.currentIsland == null || LocationUtils.currentZone == null) {
            currentSlayer = Slayer.Unknown;
            return;
        }

        Map<String, Slayer> islandMapping = SLAYER_MAPPING.get(LocationUtils.currentIsland);
        if (islandMapping != null) {
            currentSlayer = islandMapping.getOrDefault(LocationUtils.currentZone, Slayer.Unknown);
        } else {
            currentSlayer = Slayer.Unknown;
        }
    }

    public static void updateSlayerState() {
        boolean currentlyInCombat = ScoreboardUtils.scoreboardContains("Slay the boss!");

        if (currentlyInCombat && !inCombat) {
            inCombat = true;
            startTime = System.currentTimeMillis();
        } else if (!currentlyInCombat && inCombat) {
            inCombat = false;
            if (killTime) Utils.modMessage("Slayer took §6" + getKillTime() + "§7 to kill!");
            startTime = 0L;
        }

        if (ScoreboardUtils.scoreboardContains("Slayer Quest")) {
            if (ScoreboardUtils.scoreboardContains("Boss slain!")) {
                currentSlayerState = SlayerState.BOSS_SLAIN;
            } else if (ScoreboardUtils.scoreboardContains("Slay the boss!")) {
                currentSlayerState = SlayerState.IN_COMBAT;
            } else if (hasCombatXPLine()) {
                currentSlayerState = SlayerState.SUMMONING_BOSS;
            }
        } else {
            currentSlayerState = SlayerState.NOT_IN_SLAYER;
        }
    }

    private static boolean hasCombatXPLine() {
        List<String> scoreboard = ScoreboardUtils.getScoreboard();
        for (String line : scoreboard) {
            String cleanLine = ScoreboardUtils.cleanSB(line);
            if (cleanLine.matches("\\(\\d{1,3}(?:,\\d{3})*/\\d{1,3}(?:,\\d{3})*k?\\) Combat")) {
                return true;
            }
        }
        return false;
    }

    public static String getKillTime() {
        long durationMillis = System.currentTimeMillis() - startTime;
        double seconds = durationMillis / 1000.0;
        return String.format("%.2fs", seconds);
    }

    public static Slayer getCurrentSlayer() {
        return currentSlayer;
    }

    public static SlayerState getCurrentSlayerState() {
        return currentSlayerState;
    }
}
