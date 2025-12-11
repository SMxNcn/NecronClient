package cn.boop.necron.config.script.actions;

import cn.boop.necron.Necron;
import cn.boop.necron.config.script.Action;
import cn.boop.necron.utils.Utils;
import net.minecraft.util.ChatComponentText;

public class SendChatComponentAction implements Action {
    private final String message;
    private final long delay;
    private final boolean isModMsg;

    public SendChatComponentAction(String message, long delay, boolean isModMsg) {
        this.message = message;
        this.delay = delay;
        this.isModMsg = isModMsg;
    }
    @Override
    public void execute() {
        if (isModMsg) Utils.modMessage(message);
        else Necron.mc.thePlayer.addChatComponentMessage(new ChatComponentText(message));
    }

    @Override
    public long getDelay() {
        return delay;
    }
}
