package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.config.impl.DungeonOptionsImpl;
import cn.boop.necron.utils.PlayerUtils;
import cn.boop.necron.utils.RenderUtils;
import cn.boop.necron.utils.RotationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.autoI4;
import static cn.boop.necron.config.impl.DungeonOptionsImpl.rodSlot;

public class AutoI4 {
    public static final AutoI4 INSTANCE = new AutoI4();
    private boolean isRunning = false;
    private int hitCount = 0;
    private BlockPos currentEmeraldPos = null;
    private final Set<BlockPos> hitPositions = new HashSet<>();
    private boolean isShooting = false;
    private boolean deviceCompleted = false;

    private boolean isInterrupted = false;
    private long interruptStartTime = 0;
    private static final long MAX_INTERRUPT_TIME = 1500;

    private static final Pattern DEV_COMPLETE_PATTERN = Pattern.compile("(\\w+) completed a device! \\((.*?)\\)");
    private static final Pattern DEV_FAILED_PATTERN = Pattern.compile("☠ (\\w{1,16}) .* and became a ghost\\.");
    private static final Pattern BONZO_PATTERN = Pattern.compile("^Your (?:. )?Bonzo's Mask saved your life!$");
    private static final Pattern SPIRIT_PATTERN = Pattern.compile("^Second Wind Activated! Your Spirit Mask saved your life!$");
    private static final Pattern PHOENIX_PATTERN = Pattern.compile("^Your Phoenix Pet saved you from certain death!$");

    private static final BlockPos PLATE_POS = new BlockPos(63, 127, 35);

    private static final BlockPos[] TARGET_POSITIONS = {
            new BlockPos(64, 126, 50), new BlockPos(66, 126, 50), new BlockPos(68, 126, 50),
            new BlockPos(64, 128, 50), new BlockPos(66, 128, 50), new BlockPos(68, 128, 50),
            new BlockPos(64, 130, 50), new BlockPos(66, 130, 50), new BlockPos(68, 130, 50)
    };

    private static final Vec3[] AIMING_POSITIONS = {
            new Vec3(67.3, 131.1, 48.8),
            new Vec3(65.5, 131.1, 48.7),
            new Vec3(67.3, 129.1, 48.8),
            new Vec3(65.4, 129.1, 48.7),
            new Vec3(67.3, 127.5, 48.8),
            new Vec3(65.5, 127.3, 48.7)
    };

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Necron.mc.theWorld == null || Necron.mc.thePlayer == null) return;
        if (!autoI4 || event.phase != TickEvent.Phase.START) return;

        if (isInterrupted && System.currentTimeMillis() - interruptStartTime > MAX_INTERRUPT_TIME) {
            resumeShooting();
        }

        if (!isRunning) {
            checkDeviceActivation();
        } else if (atDevice() && !isInterrupted) {
            detectEmeraldBlock();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        renderCircleIndicator(event.partialTicks);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        Matcher devCompleteMatcher = DEV_COMPLETE_PATTERN.matcher(message);
        Matcher devFailedMatcher = DEV_FAILED_PATTERN.matcher(message);
        Matcher bonzoMatcher = BONZO_PATTERN.matcher(message);
        Matcher spiritMatcher = SPIRIT_PATTERN.matcher(message);
        Matcher phoenixMatcher = PHOENIX_PATTERN.matcher(message);

        if (devCompleteMatcher.matches() && !deviceCompleted) {
            deviceCompleted = true;
            completeDetection();
            return;
        }

        if (devFailedMatcher.matches()) {
            deviceCompleted = false;
            stopDetection();
        }

        if (bonzoMatcher.matches()) {
            System.out.println("Bonzo proceed!");
            int spiritSlot = findMaskItems("SPIRIT");
            Utils.modMessage("Spirit Slot: " + spiritSlot);

            /*interruptShooting();
            Utils.chatMessage("/eq");
            Necron.mc.addScheduledTask(() -> {
                try {
                    Thread.sleep(160 + Utils.random.nextInt(80));
                    if (Utils.clickInventorySlot(spiritSlot)) {
                        Thread.sleep(160 + Utils.random.nextInt(80));
                        Necron.mc.thePlayer.closeScreen();
                        Thread.sleep(Utils.random.nextInt(50) + 50);
                        resumeShooting();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return;*/
        }

        if (spiritMatcher.matches()) {
            System.out.println("Spirit proceed!");
            if (!isNormalRodSlot(DungeonOptionsImpl.rodSlot)) return;
            Utils.modMessage("Rod Slot: " + rodSlot);

            /*interruptShooting();
            new Thread(() -> {
                try {
                    Thread.sleep(220 + Utils.random.nextInt(80));
                    Necron.mc.thePlayer.inventory.currentItem = rodSlot;
                    Thread.sleep(220 + Utils.random.nextInt(80));
                    KeyBinding.onTick(Necron.mc.gameSettings.keyBindUseItem.getKeyCode());
                    Thread.sleep(220 + Utils.random.nextInt(80));
                    Necron.mc.thePlayer.inventory.currentItem = 0;
                    Thread.sleep(Utils.random.nextInt(50) + 50);
                    resumeShooting();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();*/
        }
    }

    public int findMaskItems(String maskName) {
        for (int i = 0; i < Necron.mc.thePlayer.inventory.getSizeInventory(); i++) {
            ItemStack stack = Necron.mc.thePlayer.inventory.getStackInSlot(i);

            if (stack != null) {
                String itemID = Utils.getSkyBlockID(stack);

                switch (maskName) {
                    case "BONZO":
                        if (itemID.contains("BONZO_MASK")) {
                            return i;
                        }
                        break;
                    case "SPIRIT":
                        if (itemID.contains("SPIRIT_MASK")) {
                            return i;
                        }
                        break;
                }
            }
        }
        return -1;
    }

    public void onBlockChangePacket(S23PacketBlockChange packet) {
        if (!autoI4 || !isRunning || !atDevice()) return;

        BlockPos blockPos = packet.getBlockPosition();

        if (isInTargetArea(blockPos)) {
            Block block = packet.getBlockState().getBlock();

            if (block == Blocks.emerald_block && !hitPositions.contains(blockPos)) {
                if (!isShooting) {
                    currentEmeraldPos = blockPos;
                    startShootingSequence();
                }
            }
            else if (block != Blocks.emerald_block && blockPos.equals(currentEmeraldPos)) {
                handleEmeraldHit();
            }
        }
    }

    private void startShootingSequence() {
        if (currentEmeraldPos == null || isShooting || !atDevice()) return;

        isShooting = true;
        Vec3 aimPos = selectBestAimingPosition();
        RotationUtils.asyncAimAt(aimPos, 0.3f);

        new Thread(() -> {
            try {
                if (!atDevice()) {
                    isShooting = false;
                    return;
                }

                if (isInterrupted) return;

                PlayerUtils.leftClick();
                isShooting = false;
                int clickDelay = 85 + Utils.random.nextInt(26);
                Thread.sleep(clickDelay);

                Block currentBlock = Necron.mc.theWorld.getBlockState(currentEmeraldPos).getBlock();
                if (currentBlock != Blocks.emerald_block && currentBlock != null) {
                    handleEmeraldHit();
                } else {
                    currentEmeraldPos = null;
                }
            } catch (InterruptedException e) {
                isShooting = false;
            } catch (NullPointerException ignore) {}
        }).start();
    }

    private boolean isInTargetArea(BlockPos pos) {
        for (BlockPos targetPos : TARGET_POSITIONS) {
            if (pos.equals(targetPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTargetAreaValid() {
        int blueTerracottaCount = 0;
        for (BlockPos pos : TARGET_POSITIONS) {
            Block block = Necron.mc.theWorld.getBlockState(pos).getBlock();
            if (block == Blocks.stained_hardened_clay) {
                blueTerracottaCount++;
            }
        }
        return blueTerracottaCount >= 6;
    }

    private void checkDeviceActivation() {
        if (atDevice() && isTargetAreaValid()) {
            startDetection();
        }
    }

    private void startDetection() {
        if (isRunning) return;
        isRunning = true;
        hitCount = 0;
        hitPositions.clear();
        currentEmeraldPos = null;
    }

    private void detectEmeraldBlock() {
        if (isShooting || isInterrupted) return;

        if (currentEmeraldPos != null) {
            Block currentBlock = Necron.mc.theWorld.getBlockState(currentEmeraldPos).getBlock();
            if (currentBlock != Blocks.emerald_block && !hitPositions.contains(currentEmeraldPos)) {
                handleEmeraldHit();
                return;
            }
        }

        BlockPos detectedEmerald = null;
        for (BlockPos pos : TARGET_POSITIONS) {
            if (hitPositions.contains(pos)) continue;

            Block block = Necron.mc.theWorld.getBlockState(pos).getBlock();
            if (block == Blocks.emerald_block) {
                detectedEmerald = pos;
                break;
            }
        }

        if (detectedEmerald != null && !detectedEmerald.equals(currentEmeraldPos)) {
            currentEmeraldPos = detectedEmerald;
            startShootingSequence();
        }
    }

    private void handleEmeraldHit() {
        if (isInterrupted) return;
        BlockPos emeraldPos = currentEmeraldPos;
        if (emeraldPos == null) return;
        if (!hitPositions.contains(emeraldPos)) {
            hitPositions.add(emeraldPos);
            hitCount++;
            currentEmeraldPos = null;

            int totalTargets = 9;
            if (hitCount < totalTargets) {
                detectEmeraldBlock();
            }
        }
    }

    public void renderCircleIndicator(float partialTicks) {
        if (!autoI4) return;

        double centerX = PLATE_POS.getX() + 0.5;
        double centerY = PLATE_POS.getY() + 0.5;
        double centerZ = PLATE_POS.getZ() + 0.5;

        double playerX = Necron.mc.thePlayer.lastTickPosX + (Necron.mc.thePlayer.posX - Necron.mc.thePlayer.lastTickPosX) * partialTicks;
        double playerY = Necron.mc.thePlayer.lastTickPosY + (Necron.mc.thePlayer.posY - Necron.mc.thePlayer.lastTickPosY) * partialTicks;
        double playerZ = Necron.mc.thePlayer.lastTickPosZ + (Necron.mc.thePlayer.posZ - Necron.mc.thePlayer.lastTickPosZ) * partialTicks;

        double horizontalDistance = Math.sqrt(Math.pow(playerX - centerX, 2) + Math.pow(playerZ - centerZ, 2));
        double verticalDistance = Math.abs(playerY - centerY);

        double detectionRadius = 1.5f;
        boolean inRange = horizontalDistance <= detectionRadius && verticalDistance <= 0.55f;

        Color color = inRange ? new Color(0, 255, 0) : new Color(255, 165, 0);

        RenderUtils.drawCircleOnBlock(
                PLATE_POS.getX(),
                PLATE_POS.getY() - 1.0f,
                PLATE_POS.getZ(),
                color,
                2.0f,
                (float) detectionRadius,
                partialTicks
        );
    }

    private boolean atDevice() {
        if (Necron.mc.thePlayer == null) return false;

        double x = Necron.mc.thePlayer.posX;
        double y = Necron.mc.thePlayer.posY;
        double z = Necron.mc.thePlayer.posZ;

        return x >= 62.0 && x <= 65.0 &&
                y == 127.0 &&
                z >= 34.0 && z <= 37.0;
    }

    private Vec3 selectBestAimingPosition() {
        if (currentEmeraldPos == null) return AIMING_POSITIONS[2];

        int x = currentEmeraldPos.getX();
        int y = currentEmeraldPos.getY();

        switch (x) {
            case 68:
                switch (y) {
                    case 130: return AIMING_POSITIONS[0];
                    case 128: return AIMING_POSITIONS[2];
                    case 126: return AIMING_POSITIONS[4];
                }
                break;
            case 64:
                switch (y) {
                    case 130: return AIMING_POSITIONS[1];
                    case 128: return AIMING_POSITIONS[3];
                    case 126: return AIMING_POSITIONS[5];
                }
                break;
            case 66:
                switch (y) {
                    case 130: return AIMING_POSITIONS[0];
                    case 128: return AIMING_POSITIONS[2];
                    case 126: return AIMING_POSITIONS[4];
                }
                break;
        }

        return AIMING_POSITIONS[2];
    }

    private void completeDetection() {
        isRunning = false;
        isInterrupted = false;
        currentEmeraldPos = null;
        Utils.modMessage("§ai4 Completed! Leap to: [" + "§a]");
        deviceCompleted = false;
    }

    public void stopDetection() {
        isRunning = false;
        isInterrupted = false;
        currentEmeraldPos = null;
        hitPositions.clear();
        hitCount = 0;
        Utils.modMessage("§ai4 Incompleted!");
    }

    public void interruptShooting() {
        if (isShooting || isRunning) {
            isInterrupted = true;
            interruptStartTime = System.currentTimeMillis();
        }
    }

    public void resumeShooting() {
        if (isInterrupted) {
            isInterrupted = false;
            interruptStartTime = 0;

            if (isRunning && atDevice()) {
                detectEmeraldBlock();
            }
        }
    }

    private static boolean isNormalRodSlot(int slot) {
            ItemStack stack = Necron.mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() != null) {
                if (stack.getItem() == Items.fishing_rod) {
                    String skyBlockId = Utils.getSkyBlockID(stack);
                    return !"FLAMING_FLAY".equals(skyBlockId) && !"SOUL_WHIP".equals(skyBlockId);
                }
            }
        return false;
    }
}