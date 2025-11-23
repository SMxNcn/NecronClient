package cn.boop.necron.events;

import cn.boop.necron.module.impl.FakeWipe;
import cn.boop.necron.utils.B64Utils;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Timer;
import java.util.TimerTask;

public class B64ChatEventHandler {
    private static String decoded = "";

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();

        if (message.startsWith("Necron »")) return;
        String cleanMessage = Utils.removeFormatting(message);

        int startIndex = cleanMessage.indexOf("::");
        if (startIndex != -1) {
            int endIndex = cleanMessage.indexOf("%]", startIndex);
            if (endIndex != -1) {
                String encodedPart = cleanMessage.substring(startIndex, endIndex + 2);
                decoded = B64Utils.decodeWithOffset(encodedPart);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (decoded != null) {
                            boolean canTrigger = decoded.contains(FakeWipe.triggerMsg);
                            if (!canTrigger) Utils.modMessage("§aDecoded message:§f " + decoded);
                            else FakeWipe.triggerBanMsg();
                        } else {
                            Utils.modMessage("§cFailed to decode message: Invalid format.");
                        }
                    }
                }, 100);
            }
        }
    }
}
