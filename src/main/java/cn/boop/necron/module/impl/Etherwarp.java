package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.ItemUtils;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.PlayerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.boop.necron.config.impl.EtherwarpOptionsImpl.etherwarp;
import static cn.boop.necron.config.impl.RouterOptionsImpl.router;

public class Etherwarp {
    private long lastMouseClickTime = 0;
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
    public void onMouseInput(MouseEvent event) {
        if (Necron.mc.thePlayer == null || !etherwarp || !LocationUtils.inSkyBlock) return;
        boolean currentlyInGui = Necron.mc.currentScreen != null;

        if (wasInGui && !currentlyInGui) guiCloseDelay = 10;
        wasInGui = currentlyInGui;

        if (currentlyInGui || guiCloseDelay > 0) return;

        if (event.button == 0 && event.buttonstate) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMouseClickTime < 50) return;

            ItemStack currentItem = Necron.mc.thePlayer.inventory.getCurrentItem();
            if (isEtherwarpItem(currentItem)) {
                if (EtherwarpRouter.waypointCache.isEmpty() || EtherwarpRouter.currentWaypointIndex == -1 || !router) {
                    useEtherwarp(false);
                    lastMouseClickTime = currentTime;
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!etherwarp || !LocationUtils.inSkyBlock) return;
        if (guiCloseDelay > 0) guiCloseDelay--;

        wasInGui = Necron.mc.currentScreen != null;
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
        String itemID = ItemUtils.getSkyBlockID(itemStack);
        boolean isCorrectItem = "ASPECT_OF_THE_END".equals(itemID) || "ASPECT_OF_THE_VOID".equals(itemID);

        NBTTagCompound extraAttributes = itemStack.getSubCompound("ExtraAttributes", false);
        boolean hasEthermerge = false;
        if (extraAttributes != null && extraAttributes.hasKey("ethermerge")) {
            hasEthermerge = extraAttributes.getBoolean("ethermerge");
        }

        return isCorrectItem && hasEthermerge;
    }
}
