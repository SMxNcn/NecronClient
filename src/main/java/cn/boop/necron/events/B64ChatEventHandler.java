package cn.boop.necron.events;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.FakeWipe;
import cn.boop.necron.utils.B64Utils;
import cn.boop.necron.utils.Utils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Timer;
import java.util.TimerTask;

public class B64ChatEventHandler {
    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
        String message = event.message.getFormattedText();

        if (message.startsWith("Necron »") || message.startsWith("N »")) return;

        String cleanMessage = Utils.removeFormatting(message);

        int startIndex = cleanMessage.indexOf("::");
        if (startIndex != -1) {
            int endIndex = cleanMessage.indexOf("%]", startIndex);
            if (endIndex != -1) {
                String encodedPart = cleanMessage.substring(startIndex, endIndex + 2);
                final String decodedResult = B64Utils.decodeWithOffset(encodedPart);

                if (decodedResult == null) return;
                event.setCanceled(true);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (decodedResult.contains(FakeWipe.triggerMsg)) {
                                FakeWipe.triggerBanMsg();
                                return;
                            }

                            String finalOutput = "§bN §8»§r " + message.replace(encodedPart, decodedResult);
                            Necron.mc.thePlayer.addChatMessage(new ChatComponentText(finalOutput));

                        } catch (Exception e) {
                            Necron.LOGGER.error("Error decoding message: {}", e.getMessage());
                        }
                    }
                }, 50);
            }
        }
    }
}
