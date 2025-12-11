package cn.boop.necron.config.script.actions;

import cn.boop.necron.config.script.Action;
import cn.boop.necron.utils.Utils;

public class SendChatAction implements Action {
    private final String message;
    private final long delay;
    private final boolean isCommand;

    public SendChatAction(String message, long delay, boolean isCommand) {
        this.message = message;
        this.delay = delay;
        this.isCommand = isCommand;
    }

    @Override
    public void execute() {
        Utils.chatMessage(isCommand ? "/" + message : message);
    }

    @Override
    public long getDelay() {
        return delay;
    }
}
