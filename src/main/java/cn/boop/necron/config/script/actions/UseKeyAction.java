package cn.boop.necron.config.script.actions;

import cn.boop.necron.config.script.Action;
import cn.boop.necron.utils.PlayerUtils;

public class UseKeyAction implements Action {
    private final int keyCode;
    private final long delay;

    public UseKeyAction(int keyCode, long delay) {
        this.keyCode = keyCode;
        this.delay = delay;
    }

    @Override
    public void execute() {
        PlayerUtils.pressKeyOnce(keyCode);
    }

    @Override
    public long getDelay() {
        return delay;
    }
}
