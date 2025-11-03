package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.config.impl.RouterOptionsImpl;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.PlayerUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.boop.necron.config.impl.EtherwarpOptionsImpl.etherwarp;

public class Etherwarp {
    private boolean lastLeftClick = false;
    private boolean wasInGui = false;
    private int guiCloseDelay = 0;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {
    private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "Etherwarp-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!etherwarp || !LocationUtils.inSkyBlock) return;

        boolean currentLeftClick = Mouse.isButtonDown(0);
        boolean currentlyInGui = Necron.mc.currentScreen != null;

        if (wasInGui && !currentlyInGui) guiCloseDelay = 10;
        wasInGui = currentlyInGui;

        if (currentlyInGui) {
            if (guiCloseDelay > 0) {
                guiCloseDelay--;
            }
            lastLeftClick = currentLeftClick;
            return;
        }

        if (Necron.mc.thePlayer.inventory.getCurrentItem() == null || !isEtherwarpItem(Necron.mc.thePlayer.inventory.getCurrentItem())) return;

        if (EtherwarpRouter.waypointCache.isEmpty() || EtherwarpRouter.currentWaypointIndex == -1 || !RouterOptionsImpl.router) {
            if (!lastLeftClick && currentLeftClick) useEtherwarp(false);
        }
        lastLeftClick = currentLeftClick;
    }

    public static void useEtherwarp(boolean sneak) {
        executor.submit(() -> {
            try {
                if (sneak) {
                    PlayerUtils.setSneak(true);
                    Thread.sleep(100);
                    PlayerUtils.rightClick();
                    Thread.sleep(50);
                } else {
                    PlayerUtils.setSneak(true);
                    Thread.sleep(100);
                    PlayerUtils.rightClick();
                    Thread.sleep(50);
                    PlayerUtils.setSneak(false);
                }
            } catch (InterruptedException e) {
                Necron.LOGGER.error(e);
            }
        });
    }

    public static boolean isEtherwarpItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        String itemID = Utils.getSkyBlockID(itemStack);
        boolean isCorrectItem = "ASPECT_OF_THE_END".equals(itemID) || "ASPECT_OF_THE_VOID".equals(itemID);

        NBTTagCompound extraAttributes = itemStack.getSubCompound("ExtraAttributes", false);
        boolean hasEthermerge = false;
        if (extraAttributes != null && extraAttributes.hasKey("ethermerge")) {
            hasEthermerge = extraAttributes.getBoolean("ethermerge");
        }

        return isCorrectItem && hasEthermerge;
    }
}
