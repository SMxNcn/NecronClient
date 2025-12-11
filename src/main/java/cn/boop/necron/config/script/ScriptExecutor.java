package cn.boop.necron.config.script;

public class ScriptExecutor {
    public static void executeScript(Script script) {
        new Thread(() -> {
            try {
                if (script.getInitialDelay() > 0) {
                    Thread.sleep(script.getInitialDelay());
                }

                for (Action action : script.getActions()) {
                    if (!script.isEnabled()) break;

                    action.execute();

                    long delay = action.getDelay();
                    if (delay > 0) {
                        Thread.sleep(delay);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ScriptExecutor-" + script.getName()).start();
    }
}
