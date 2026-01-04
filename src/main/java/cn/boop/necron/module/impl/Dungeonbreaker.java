package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.betterDgb;
import static cn.boop.necron.config.impl.DungeonOptionsImpl.switchKey;

public class Dungeonbreaker {
    private int dgbSlot = -1;
    private int lastSlot = -1;
    private boolean canSwitch = false;
    private boolean switched = false;
    private boolean msgSent = false;
    private long lastSwitchTime = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Necron.mc.thePlayer == null || !betterDgb || Necron.mc.currentScreen != null || AutoI4.INSTANCE.isSwitching) return;

        boolean keyPressed = switchKey.isActive();

        if (keyPressed != canSwitch) {
            if (System.currentTimeMillis() - lastSwitchTime < Utils.random.nextInt(61) + 160) return;

            canSwitch = keyPressed;

            if (canSwitch) {
                lastSlot = Necron.mc.thePlayer.inventory.currentItem;
                msgSent = false;
                switchToDgb();
                lastSwitchTime = System.currentTimeMillis();
                switched = true;
            } else {
                if (switched && lastSlot != -1) {
                    Necron.mc.thePlayer.inventory.currentItem = lastSlot;
                    lastSwitchTime = System.currentTimeMillis();
                }
                switched = false;
                dgbSlot = -1;
            }
        }

        if (canSwitch && dgbSlot == -1) {
            findDgbSlot();
        }
    }

    private void findDgbSlot() {
        dgbSlot = -1;
        for (int i = 0; i < 8; i++) {
            ItemStack itemStack = Necron.mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null && Utils.getSkyBlockID(itemStack).equals("DUNGEONBREAKER")) {
                dgbSlot = i;
                break;
            }
        }
    }

    private void switchToDgb() {
        if (dgbSlot == -1) findDgbSlot();
        if (dgbSlot != -1) {
            Necron.mc.thePlayer.inventory.currentItem = dgbSlot;
        } else if (!msgSent) {
            Utils.modMessage("§cDungeonbreaker not found in hotbar!");
            msgSent = true;
        }
    }
}
