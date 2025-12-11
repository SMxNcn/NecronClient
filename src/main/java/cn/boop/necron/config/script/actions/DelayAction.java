package cn.boop.necron.config.script.actions;

import cn.boop.necron.config.script.Action;

public class DelayAction implements Action {
    private final long milliseconds;

    public DelayAction(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public long getDelay() {
        return milliseconds;
    }
}
