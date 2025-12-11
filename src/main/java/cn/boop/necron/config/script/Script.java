package cn.boop.necron.config.script;

import java.util.ArrayList;
import java.util.List;

public class Script {
    private String name;
    private int triggerKey;
    private long initialDelay;
    private List<Action> actions;
    private boolean enabled;

    public Script(String name, int triggerKey, long initialDelay, boolean enabled) {
        this.name = name;
        this.triggerKey = triggerKey;
        this.initialDelay = initialDelay;
        this.actions = new ArrayList<>();
        this.enabled = enabled;
    }

    public void setTriggerKey(int triggerKey) {
        this.triggerKey = triggerKey;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTriggerKey() {
        return triggerKey;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public List<Action> getActions() {
        return actions;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
