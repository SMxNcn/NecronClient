package cn.boop.necron.config;

public enum ResetReason {
    WORLD_CHANGE("Detection server changed"),
    TELEPORT("Detection position changed"),
    //MOTION("Detection incorrect movement");
    UNLOAD("Waypoint unloaded");

    private final String message;

    ResetReason(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
