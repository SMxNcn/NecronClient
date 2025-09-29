package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.PlayerUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import static cn.boop.necron.config.impl.EtherwarpOptionsImpl.etherwarp;

public class Etherwarp {
    private boolean lastLeftClick = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        boolean currentLeftClick = Mouse.isButtonDown(0);
        boolean ethermerge = false;

        if (etherwarp && LocationUtils.inSkyBlock && Necron.mc.currentScreen == null) {
            if (Necron.mc.thePlayer.inventory.getCurrentItem() == null) return;
            NBTTagCompound extraAttributes = Necron.mc.thePlayer.inventory.getCurrentItem().getSubCompound("ExtraAttributes", false);
            if (extraAttributes != null && extraAttributes.hasKey("ethermerge")) {
                ethermerge = extraAttributes.getBoolean("ethermerge");
            }

            if (EtherwarpRouter.waypointCache.isEmpty() || EtherwarpRouter.currentWaypointIndex == -1) {
                if (!lastLeftClick && currentLeftClick && ethermerge) {
                    useEtherwarp();
                }
            }
        }
        lastLeftClick = currentLeftClick;
    }

    public static void useEtherwarp() {
        if (Necron.mc.currentScreen != null) return;
        String itemID = Utils.getSkyBlockID(Necron.mc.thePlayer.inventory.getCurrentItem());
        if (itemID.equals("ASPECT_OF_THE_END") || itemID.equals("ASPECT_OF_THE_VOID")) {
            new Thread(() -> {
                try {
                    PlayerUtils.setSneak(true);
                    Thread.sleep(100);
                    PlayerUtils.rightClick();
                    Thread.sleep(50);
                    PlayerUtils.setSneak(false);
                } catch (InterruptedException e) {
                    Necron.LOGGER.error(e);
                }
            }, "Etherwarp").start();
        }
    }
}
