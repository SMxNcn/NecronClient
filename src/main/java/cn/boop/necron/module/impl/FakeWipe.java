package cn.boop.necron.module.impl;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cn.boop.necron.Necron;
import cn.boop.necron.utils.LocationUtils;
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

public class FakeWipe {
    public static boolean hasTriggered = false;
    private boolean wasOnHypixel = false;
    private int tickCounter = 0;

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
