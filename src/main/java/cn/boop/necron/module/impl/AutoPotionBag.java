package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Timer;
import java.util.TimerTask;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.autoPotionBag;

public class AutoPotionBag {
    private static boolean pbOpened = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!autoPotionBag) return;
        String message = event.message.getUnformattedText();
        if ((Necron.mc.thePlayer.getName() + " is now ready!").contains(message)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Utils.chatMessage("/potionbag");
                    pbOpened = true;
                    System.out.println("Potion bag opened!");
                }
            }, 200);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (pbOpened) pbOpened = false;
    }
}
