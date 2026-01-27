package cn.boop.necron.module.impl.autoleap;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.AutoLeap;
import cn.boop.necron.utils.DungeonUtils;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.PlayerUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.AutoLeapOptionsImpl.autoLeap;

public class LeapRules {
    private static final Pattern LEAP_PATTERN = Pattern.compile("^You have teleported to (\\w{1,16})!$");
    private static int cooldownTicks = 0;
    private static boolean hasActiveLeapItem = false;
    private static boolean shouldCheckLeap = false;
    private static int leapCheckDelay = 0;

    private static boolean processingLeftClickLeap = false;

    private static final Map<DungeonUtils.DungeonClass, Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>>> ruleTable = new EnumMap<>(DungeonUtils.DungeonClass.class);

    static {
        initializeRuleTable();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Necron.mc.thePlayer == null || !autoLeap) return;

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        checkForLeapItem();
        if (Necron.mc.thePlayer.ticksExisted % 20 == 0) shouldCheckLeap = AutoLeap.inLeapGui;

        if (leapCheckDelay > 0) {
            leapCheckDelay--;

            if (leapCheckDelay == 0 && shouldCheckLeap && hasActiveLeapItem) {
                DungeonUtils.DungeonClass targetClass = selectLeapTarget();

                if (targetClass != null) {
                    AutoLeap.leapToClass(targetClass);
                    cooldownTicks = 40;
                }
            }
        }

        detectLeapOpportunity();
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (Necron.mc.thePlayer == null || !autoLeap) return;
        if (event.button == 0 && event.buttonstate) {
            if (hasActiveLeapItem) {
                if (cooldownTicks <= 0) startLeftClickAutoLeap();
                if (autoLeap) event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!autoLeap) return;
        String message = event.message.getUnformattedText();
        Matcher matcher = LEAP_PATTERN.matcher(message);

        if (matcher.matches()) {
            shouldCheckLeap = false;
            leapCheckDelay = 0;
            processingLeftClickLeap = false;
            cooldownTicks = 40;
        }
    }

    private static void startLeftClickAutoLeap() {
        if (!hasActiveLeapItem || !LocationUtils.inDungeon) return;

        DungeonUtils.DungeonClass targetClass = selectLeapTarget();
        boolean hasTeammate = false;
        for (DungeonUtils.DungeonPlayer player : DungeonUtils.dungeonPlayers.values()) {
            if (player.getPlayerClass() == targetClass) {
                hasTeammate = true;
                break;
            }
        }

        if (!hasTeammate || targetClass == null) {
            Utils.modMessage("§cFailed to leap: no teammate found.");
            processingLeftClickLeap = false;
            return;
        }

        processingLeftClickLeap = true;
        PlayerUtils.rightClick();
        new Thread(() -> {
            try {
                Thread.sleep(300);
                AutoLeap.leapToClass(targetClass);
                processingLeftClickLeap = false;
            } catch (NullPointerException e) {
                Utils.modMessage("§cFailed to leap: target class is null.");
            } catch (InterruptedException ignore) {}
        }).start();
    }

    private static DungeonUtils.DungeonClass getCurrentPlayerClass() {
        String currentPlayerName = Necron.mc.thePlayer.getName();
        DungeonUtils.DungeonPlayer player = DungeonUtils.dungeonPlayers.get(currentPlayerName);

        if (player != null) return player.getPlayerClass();

        String cleanName = Utils.clearMcUsername(currentPlayerName);
        for (DungeonUtils.DungeonPlayer dungeonPlayer : DungeonUtils.dungeonPlayers.values()) {
            if (dungeonPlayer.getPlayerName().equalsIgnoreCase(cleanName)) {
                return dungeonPlayer.getPlayerClass();
            }
        }

        return null;
    }

    private static void checkForLeapItem() {
        if (Necron.mc.thePlayer == null || Necron.mc.thePlayer.inventory == null || Necron.mc.thePlayer.getHeldItem() == null) {
            hasActiveLeapItem = false;
            return;
        }

        hasActiveLeapItem = AutoLeap.isLeapItem(Necron.mc.thePlayer.getHeldItem());
    }

    private static DungeonUtils.DungeonClass selectLeapTarget() {
        if (!LocationUtils.inDungeon) return null;

        if (LocationUtils.inBossRoom && LocationUtils.floor == LocationUtils.Floor.MASTER_7) {
            return handleM7Phase();
        } else if (!LocationUtils.inBossRoom) {
            return handlePreBossPhase();
        }

        return null;
    }

    private static DungeonUtils.DungeonClass handlePreBossPhase() {
        DungeonUtils.DungeonClass currentClass = getCurrentPlayerClass();

        if (currentClass == null) return null;
        if (currentClass == DungeonUtils.DungeonClass.Archer) {
            return DungeonUtils.DungeonClass.Mage;
        } else if (currentClass == DungeonUtils.DungeonClass.Mage) {
            return DungeonUtils.DungeonClass.Archer;
        }

        return null;
    }

    private static DungeonUtils.DungeonClass handleM7Phase() {
        DungeonUtils.DungeonClass currentClass = getCurrentPlayerClass();
        LocationUtils.M7Phases currentPhase = LocationUtils.getM7Phase();
        LocationUtils.P3Stages p3Stage = LocationUtils.getP3Stage();

        if (currentClass == null || currentPhase == LocationUtils.M7Phases.Unknown) return null;

        boolean isCore = false;
        if (p3Stage == LocationUtils.P3Stages.S3) {
            try {
                isCore = checkIfMageIsCore();
            } catch (NullPointerException e) {
                Utils.modMessage("§cFailed to query rule: Mage position is null.");
                isCore = true;
            }
        }
        return queryRule(currentClass, currentPhase, p3Stage, isCore);
    }

    private static void initializeRuleTable() {
        // Archer
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> archerRules = new EnumMap<>(LocationUtils.M7Phases.class);
        Map<LocationUtils.P3Stages, TargetRule> archerP3Rules = new EnumMap<>(LocationUtils.P3Stages.class);

        archerRules.put(LocationUtils.M7Phases.P1, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Berserk)));
        archerRules.put(LocationUtils.M7Phases.P2, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));

        archerP3Rules.put(LocationUtils.P3Stages.S2, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        archerP3Rules.put(LocationUtils.P3Stages.Tunnel, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        archerP3Rules.put(LocationUtils.P3Stages.S3, new SimpleRule(DungeonUtils.DungeonClass.Mage));
        archerP3Rules.put(LocationUtils.P3Stages.S4, new SimpleRule(DungeonUtils.DungeonClass.Mage));

        archerRules.put(LocationUtils.M7Phases.P3, archerP3Rules);
        archerRules.put(LocationUtils.M7Phases.P4, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));

        ruleTable.put(DungeonUtils.DungeonClass.Archer, archerRules);

        // Berserk
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> berserkRules = new EnumMap<>(LocationUtils.M7Phases.class);
        Map<LocationUtils.P3Stages, TargetRule> berserkP3Rules = new EnumMap<>(LocationUtils.P3Stages.class);

        berserkP3Rules.put(LocationUtils.P3Stages.S1, new SimpleRule(DungeonUtils.DungeonClass.Archer));
        berserkP3Rules.put(LocationUtils.P3Stages.S2, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        berserkP3Rules.put(LocationUtils.P3Stages.Tunnel, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        berserkP3Rules.put(LocationUtils.P3Stages.S3, new SimpleRule(DungeonUtils.DungeonClass.Mage));
        berserkP3Rules.put(LocationUtils.P3Stages.S4, new SimpleRule(DungeonUtils.DungeonClass.Mage));

        berserkRules.put(LocationUtils.M7Phases.P2, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));
        berserkRules.put(LocationUtils.M7Phases.P3, berserkP3Rules);
        berserkRules.put(LocationUtils.M7Phases.P4, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));

        ruleTable.put(DungeonUtils.DungeonClass.Berserk, berserkRules);

        // Healer
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> healerRules = new EnumMap<>(LocationUtils.M7Phases.class);
        Map<LocationUtils.P3Stages, TargetRule> healerP3Rules = new EnumMap<>(LocationUtils.P3Stages.class);

        healerP3Rules.put(LocationUtils.P3Stages.S1, new SimpleRule(DungeonUtils.DungeonClass.Archer));
        healerP3Rules.put(LocationUtils.P3Stages.S3, new SimpleRule(DungeonUtils.DungeonClass.Mage));
        healerP3Rules.put(LocationUtils.P3Stages.S4, new SimpleRule(DungeonUtils.DungeonClass.Mage));

        healerRules.put(LocationUtils.M7Phases.P2, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Archer)));
        healerRules.put(LocationUtils.M7Phases.P3, healerP3Rules);
        healerRules.put(LocationUtils.M7Phases.P5, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Berserk)));

        ruleTable.put(DungeonUtils.DungeonClass.Healer, healerRules);

        // Mage
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> mageRules = new EnumMap<>(LocationUtils.M7Phases.class);
        Map<LocationUtils.P3Stages, TargetRule> mageP3Rules = new EnumMap<>(LocationUtils.P3Stages.class);

        mageP3Rules.put(LocationUtils.P3Stages.S1, new SimpleRule(DungeonUtils.DungeonClass.Archer));
        mageP3Rules.put(LocationUtils.P3Stages.S2, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        mageP3Rules.put(LocationUtils.P3Stages.Tunnel, new SimpleRule(DungeonUtils.DungeonClass.Healer));

        mageRules.put(LocationUtils.M7Phases.P1, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Berserk)));
        mageRules.put(LocationUtils.M7Phases.P2, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));
        mageRules.put(LocationUtils.M7Phases.P3, mageP3Rules);
        mageRules.put(LocationUtils.M7Phases.P4, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));
        mageRules.put(LocationUtils.M7Phases.P5, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Berserk)));

        ruleTable.put(DungeonUtils.DungeonClass.Mage, mageRules);

        // Tank
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> tankRules = new EnumMap<>(LocationUtils.M7Phases.class);
        Map<LocationUtils.P3Stages, TargetRule> tankP3Rules = new EnumMap<>(LocationUtils.P3Stages.class);
        
        tankP3Rules.put(LocationUtils.P3Stages.S1, new SimpleRule(DungeonUtils.DungeonClass.Archer));
        tankP3Rules.put(LocationUtils.P3Stages.S2, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        tankP3Rules.put(LocationUtils.P3Stages.Tunnel, new SimpleRule(DungeonUtils.DungeonClass.Healer));
        tankP3Rules.put(LocationUtils.P3Stages.S3, new SimpleRule(DungeonUtils.DungeonClass.Mage));
        tankP3Rules.put(LocationUtils.P3Stages.S4, new SimpleRule(DungeonUtils.DungeonClass.Mage));

        tankRules.put(LocationUtils.M7Phases.P1, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Berserk)));
        tankRules.put(LocationUtils.M7Phases.P2, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));
        tankRules.put(LocationUtils.M7Phases.P3, tankP3Rules);
        tankRules.put(LocationUtils.M7Phases.P4, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Healer)));
        tankRules.put(LocationUtils.M7Phases.P5, Collections.singletonMap(null, new SimpleRule(DungeonUtils.DungeonClass.Archer)));

        ruleTable.put(DungeonUtils.DungeonClass.Tank, tankRules);
    }

    private static DungeonUtils.DungeonClass queryRule(DungeonUtils.DungeonClass sourceClass, LocationUtils.M7Phases phase, LocationUtils.P3Stages p3Stage, boolean isCore) {
        Map<LocationUtils.M7Phases, Map<LocationUtils.P3Stages, TargetRule>> classRules = ruleTable.get(sourceClass);
        if (classRules == null) return null;

        Map<LocationUtils.P3Stages, TargetRule> phaseRules = classRules.get(phase);
        if (phaseRules == null) return null;

        TargetRule rule = phaseRules.get(p3Stage);

        if (rule == null) rule = phaseRules.get(null);

        return rule != null ? rule.getTarget(isCore) : null;
    }

    private static boolean checkIfMageIsCore() {
        String mageName;
        BlockPos magePos = null;
        try {
            for (Map.Entry<String, DungeonUtils.DungeonPlayer> entry : DungeonUtils.dungeonPlayers.entrySet()) {
                DungeonUtils.DungeonPlayer player = entry.getValue();
                if (player != null && player.getPlayerClass() == DungeonUtils.DungeonClass.Mage) {
                    mageName = player.getPlayerName();
                    if (mageName == null) return false;
                    magePos = PlayerUtils.getPlayerPos(mageName);
                    break;
                }
            }
        } catch (Exception e) {
            Necron.LOGGER.warn(e);
        }

        if (magePos == null) return false;

        return Utils.isPlayerInArea(new BlockPos(57, 120, 53), new BlockPos(51, 114, 51), magePos);
    }

    private static void detectLeapOpportunity() {
        if (!processingLeftClickLeap) return;
        if (!hasActiveLeapItem || shouldCheckLeap || cooldownTicks > 0) return;

        DungeonUtils.DungeonClass targetClass = selectLeapTarget();
        if (targetClass != null) leapCheckDelay = 5;
    }
}
