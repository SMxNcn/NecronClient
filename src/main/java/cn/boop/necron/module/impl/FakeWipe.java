package cn.boop.necron.module.impl;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.FakeWipeOptionsImpl.fakeWipe;
import static cn.boop.necron.config.impl.FakeWipeOptionsImpl.triggerBan;

public class FakeWipe {
    public static boolean hasTriggered = false;
    private boolean wasOnHypixel = false;
    private int tickCounter = 0;
    public static final String triggerMsg = "nc.FWipeTgr";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (tickCounter++ % 20 != 0) return;

        boolean isOnHypixel = HypixelUtils.INSTANCE.isHypixel();
        if (!wasOnHypixel && isOnHypixel) {
            wasOnHypixel = true;
            triggerWipeBook();
        } else if (wasOnHypixel && !isOnHypixel) {
            wasOnHypixel = false;
            hasTriggered = false;
        }

    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if(!LocationUtils.inHypixel) wasOnHypixel = false;
    }

    private void triggerWipeBook() {
        if (hasTriggered || !fakeWipe) return;
        if (Necron.mc == null || Necron.mc.thePlayer == null || Necron.mc.theWorld == null) return;

        if (Necron.mc.isSingleplayer() || Necron.mc.getCurrentServerData() != null) {
            hasTriggered = true;

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {}

                openWipeBook();
            }).start();
        }
    }

    public static void triggerBanGui() {
        if (!triggerBan) return;
        String randomID = String.format("%08X", Utils.random.nextInt());

        ChatComponentText banMsg = new ChatComponentText("§cYou are temporarily banned for §f" + Math.max(getRandomDay(), 89) + "d 23h 59m 58s §cfrom this server!");
        banMsg.appendText("\n\n§7Reason: §rBoosting detected on one or multiple SkyBlock profiles.");
        banMsg.appendText("\n§7Find out more: §b§nhttps://www.hypixel.net/appeal");
        banMsg.appendText("\n\n§7Ban ID: §r#" + randomID);
        banMsg.appendText("\n§7Sharing your Ban ID may affect the processing of your appeal!");

        if (Necron.mc.getNetHandler() != null && Necron.mc.getNetHandler().getNetworkManager() != null) {
            Necron.mc.getNetHandler().getNetworkManager().closeChannel(banMsg);
        }
    }

    public static void triggerBanMsg() {
        if (!triggerBan) return;
        String uuid = Necron.mc.thePlayer.getUniqueID().toString();
        boolean canTrigger = "cf62bf86-3be6-4fb7-bedd-d591a1728c52".contains(uuid);

        String randomID = String.format("%07X", Utils.random.nextInt(0x10000000));
        ChatComponentText banMsg = new ChatComponentText("§cYou are temporarily banned for §f" + getRandomDay() + "d 23h 59m 58s §cfrom this server!");
        banMsg.appendText("\n\n§7Reason: §rCheating through the use of unfair game advantages.");
        banMsg.appendText("\n§7Find out more: §b§nhttps://www.hypixel.net/appeal");
        banMsg.appendText("\n\n§7Ban ID: §r#A" + randomID);
        banMsg.appendText("\n§7Sharing your Ban ID may affect the processing of your appeal!");

        if (canTrigger) {
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Utils.chatMessage("/limbo");
                    Thread.sleep(500);
                    Necron.mc.thePlayer.addChatComponentMessage(new ChatComponentText("§cAn exception occurred in your connection, so you have been routed to limbo!"));
                    Thread.sleep(2000);
                    System.out.println("Banned!");
                    Thread.sleep(500);
                    if (Necron.mc.getNetHandler() != null && Necron.mc.getNetHandler().getNetworkManager() != null) {
                        Necron.mc.getNetHandler().getNetworkManager().closeChannel(banMsg);
                    }
                } catch (InterruptedException e) {
                    Necron.LOGGER.error(e);
                }
            }).start();
        } else {
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§cA player has been removed from your game."));
                    Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§bUse /report to continue helping out the server!"));
                } catch (InterruptedException e) {
                    Necron.LOGGER.error(e);
                }
            }).start();
        }
    }

    private ItemStack getWipeBook() {
        ItemStack book = new ItemStack(Item.getItemById(386));
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("author", "Server");
        tag.setString("title", "SkyBlock Wipe Book");

        NBTTagList pages = new NBTTagList();
        pages.appendTag(new NBTTagString("\nYour SkyBlock Profile §6Necron §c§lhas been wiped§r as co-op member was determined to be boosting or cheating.\nIf you believe this to be in error, you can contact our support team:   \n§9§nsupport.hypixel.net§r\n\n        §2§lDISMISS"));

        tag.setTag("pages", pages);
        book.setTagCompound(tag);
        return book;
    }

    private static int getRandomDay() {
        int[] multipliers = {1, 3, 6, 12};
        int randomMultiplier = multipliers[Utils.random.nextInt(multipliers.length)];
        return 30 * randomMultiplier - 1;
    }

    private void openWipeBook() {
        if (Necron.mc == null || Necron.mc.thePlayer == null) return;

        ItemStack book = getWipeBook();

        Necron.mc.addScheduledTask(() -> {
            if (Necron.mc.currentScreen != null) {
                Necron.mc.displayGuiScreen(null);

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignore) {}
            }

            Necron.mc.displayGuiScreen(new GuiScreenBook(Necron.mc.thePlayer, book, false));
            Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§eYour SkyBlock Profile §bNecron §c§lhas been wiped §r§eas co-op member was determined to be boosting or cheating.\n§eIf you believe this to be in error, you can contact our support team: §b§nsupport.hypixel.net"));
        });
    }
}
