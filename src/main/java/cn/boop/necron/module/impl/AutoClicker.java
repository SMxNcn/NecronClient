package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.PlayerUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.AutoClickerOptionsImpl.*;

public class AutoClicker {
    private boolean clickPending = false;
    private long clickDelay = 0L;
    private boolean blockHitPending = false;
    private long blockHitDelay = 0L;

    private double nextLeftClick = 0.0;
    private double nextRightClick = 0.0;

    private boolean isBreakingBlock() {
        return Necron.mc.objectMouseOver != null && Necron.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private long getNextRClickDelay() {
        int minCPS = Math.min(minRCPS, maxRCPS);
        int maxCPS = Math.max(minRCPS, maxRCPS);
        if (minRCPS > maxRCPS) minRCPS = maxRCPS - 1;

        return 1000L / nextLong(minCPS, maxCPS);
    }

    private long getNextLClickDelay() {
        int minCPS = Math.min(minLCPS, maxLCPS);
        int maxCPS = Math.max(minLCPS, maxLCPS);
        if (minLCPS > maxLCPS) minLCPS = maxLCPS - 1;

        return 1000L / nextLong(minCPS, maxCPS);
    }

    public static long nextLong(long min, long max) {
        return min + (long)(Utils.random.nextDouble() * (max - min + 1));
    }

    private boolean canClick() {
        boolean leftClickAllowed = !weaponsOnly || ItemUtils.isHoldingSword();
        boolean rightClickAllowed = ItemUtils.isHoldingBlock();

        if (leftClickAllowed || rightClickAllowed) {
            if (isBreakingBlock()) {
                GameType gameType = Necron.mc.playerController.getCurrentGameType();
                if (gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE) {
                    return !ItemUtils.isHoldingTool();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!autoClicker || Necron.mc.currentScreen != null) {
            clickPending = false;
            blockHitPending = false;
            return;
        }

        if (clickDelay > 0L) clickDelay -= 50L;
        if (blockHitDelay > 0L) blockHitDelay -= 50L;

        if (clickPending) {
            clickPending = false;
            PlayerUtils.updateKeyState(Necron.mc.gameSettings.keyBindAttack.getKeyCode());
        }

        if (blockHitPending) {
            blockHitPending = false;
            PlayerUtils.updateKeyState(Necron.mc.gameSettings.keyBindUseItem.getKeyCode());
        }

    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!autoClicker) return;
        if (Necron.mc.currentScreen != null) return;
        long nowMillis = System.currentTimeMillis();

        if (leftClick && Necron.mc.gameSettings.keyBindAttack.isKeyDown() && nowMillis >= nextLeftClick) {
            if (canClick()) {
                while (clickDelay <= 0L) {
                    clickPending = true;
                    clickDelay += getNextLClickDelay();
                    PlayerUtils.setKeyBindState(Necron.mc.gameSettings.keyBindAttack.getKeyCode(), false);
                    PlayerUtils.leftClick();
                    nextLeftClick = nowMillis + clickDelay + (long)((Math.random() - 0.5) * 60.0);
                }
            }
        }

        if (rightClick && Necron.mc.gameSettings.keyBindUseItem.isKeyDown() && nowMillis >= nextRightClick) {
            if (canClick()) {
                while (blockHitDelay <= 0L) {
                    blockHitPending = true;
                    blockHitDelay += getNextRClickDelay();
                    PlayerUtils.setKeyBindState(Necron.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                    PlayerUtils.rightClick();
                    nextRightClick = nowMillis + blockHitDelay + (long)((Math.random() - 0.5) * 60.0);
                }
            }
        }
    }
}
