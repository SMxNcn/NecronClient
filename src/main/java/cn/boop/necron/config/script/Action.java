package cn.boop.necron.config.script;

public interface Action {
    void execute();
    long getDelay();
}
