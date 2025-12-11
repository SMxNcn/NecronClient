package cn.boop.necron.config.script.actions;

import cn.boop.necron.config.script.Action;
import cn.boop.necron.utils.Utils;

public class ClickSlotAction implements Action {
    private final int slot;
    private final long delay;

    public ClickSlotAction(int slot, long delay) {
        this.slot = slot;
        this.delay = delay;
    }

    @Override
    public void execute() {
        Utils.clickInventorySlot(slot);
    }

    @Override
    public long getDelay() {
        return delay;
    }
}
