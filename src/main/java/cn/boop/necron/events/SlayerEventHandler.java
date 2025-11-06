package cn.boop.necron.events;

import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.AatroxBuffChecker;
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
    private static final Pattern SLAYER_DROP_PATTERN = Pattern.compile("^§r§.§l[A-Z ]+DROP!? §r§7\\(§r§f§r(.*?)§r§7\\)");
    public static final Pattern STATUS_PATTERN = Pattern.compile("\\(\\d{1,3}(?:,\\d{3})*/\\d{1,3}(?:,\\d{3})*k?\\) (?:Combat|Kills)");
    private static final Pattern STORED_XP_PATTERN = Pattern.compile("§dRNG Meter §f- §d(\\d+) Stored XP§r");

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
        String message = event.message.getFormattedText();

        Matcher dropMatcher = SLAYER_DROP_PATTERN.matcher(message);
        Matcher storedXpMatcher = STORED_XP_PATTERN.matcher(message);

        if (dropMatcher.find()) {
            String droppedItem = dropMatcher.group(1);

            Slayer slayer = SlayerEventHandler.getCurrentSlayer();
            if (slayer != Slayer.Unknown) {
                String selectedItem = SlayerRngManager.INSTANCE.getItem(slayer.getDisplayName());

                if (selectedItem != null && selectedItem.equals(droppedItem)) {
                    int score = SlayerRngManager.INSTANCE.getScore(slayer.getDisplayName());
                    double percentage = SlayerRngManager.INSTANCE.getMeterPercentage(slayer.getDisplayName());
                    Utils.modMessage("§dRng Item §7reset! (§6" + Utils.addNumSeparator(score) + " §bScore, §6" + String.format("%.2f", percentage) + "§b%§7)");
                }

                if (storedXpMatcher.find()) {
                    int storedXp = Integer.parseInt(storedXpMatcher.group(1));
                    SlayerRngManager.INSTANCE.setScore(slayer.getDisplayName(), storedXp);
                }
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
                    islandMapping.put("The Wasteland", Slayer.Inferno);
                    islandMapping.put("Smoldering Tomb", Slayer.Inferno);
                    islandMapping.put("Burning Desert", Slayer.Tarantula);
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
            addSlayerXPToRNGMeter();
            startTime = 0L;
        }

        if (ScoreboardUtils.scoreboardContains("Slayer Quest")) {
            if (ScoreboardUtils.scoreboardContains("Boss slain!")) {
                currentSlayerState = SlayerState.BOSS_SLAIN;
            } else if (currentlyInCombat) {
                currentSlayerState = SlayerState.IN_COMBAT;
            } else if (hasCombatXPLine()) {
                currentSlayerState = SlayerState.SUMMONING_BOSS;
            }
        } else {
            currentSlayerState = SlayerState.NOT_IN_SLAYER;
        }
    }

    private static void addSlayerXPToRNGMeter() {
        Slayer slayer = getCurrentSlayer();
        if (slayer == Slayer.Unknown) return;

        int level = getSlayerLevelFromScoreboard();
        int baseXP = getSlayerXPRequirement(slayer, level);

        if (baseXP > 0) {
            double multiplier = AatroxBuffChecker.getSlayerXPMultiplier();
            int actualXP = (int) Math.round(baseXP * multiplier);

            String slayerName = slayer.getDisplayName();
            SlayerRngManager.INSTANCE.addScore(slayerName, actualXP);

            int currentScore = SlayerRngManager.INSTANCE.getScore(slayerName);
            Utils.modMessage("§dRNG Meter §7gained §6" + Utils.addNumSeparator(actualXP) + " §7XP! (§6" +
                    Utils.addNumSeparator(currentScore) + " §bScore§7)");
        }
    }

    private static boolean hasCombatXPLine() {
        List<String> scoreboard = ScoreboardUtils.getScoreboard();
        for (String line : scoreboard) {
            String cleanLine = ScoreboardUtils.cleanSB(line);
            Matcher statusMatcher = STATUS_PATTERN.matcher(cleanLine);
            if (statusMatcher.find()) {
                return true;
            }
        }
        return false;
    }

    private static int getSlayerXPRequirement(Slayer slayer, int level) {
        if (slayer == Slayer.Unknown) return 0;

        if (slayer == Slayer.Riftstalker) {
            switch (level) {
                case 1: return 10;
                case 2: return 25;
                case 3: return 60;
                case 4: return 120;
                case 5: return 160;
                default: return 0;
            }
        } else {
            switch (level) {
                case 1: return 5;
                case 2: return 25;
                case 3: return 100;
                case 4: return 500;
                case 5: return 1500;
                default: return 0;
            }
        }
    }

    private static int getSlayerLevelFromScoreboard() {
        List<String> scoreboard = ScoreboardUtils.getScoreboard();
        for (String line : scoreboard) {
            String cleanLine = ScoreboardUtils.cleanSB(line);
            if (cleanLine.contains("Revenant Horror") ||
                    cleanLine.contains("Sven Packmaster") ||
                    cleanLine.contains("Tarantula Broodfather") ||
                    cleanLine.contains("Voidgloom Seraph") ||
                    cleanLine.contains("Riftstalker Bloodfiend") ||
                    cleanLine.contains("Inferno Demonlord")) {

                String[] parts = cleanLine.split(" ");
                if (parts.length > 0) {
                    String romanNumeral = parts[parts.length - 1];
                    return Utils.romanToInt(romanNumeral);
                }
            }
        }
        return 0;
    }

    public static String getKillTime() {
        long durationMillis = System.currentTimeMillis() - startTime;
        double seconds = durationMillis / 1000.0;
        return String.format("%.2fs", seconds - 0.5);
    }

    public static Slayer getCurrentSlayer() {
        return currentSlayer;
    }

    public static SlayerState getCurrentSlayerState() {
        return currentSlayerState;
    }
}
