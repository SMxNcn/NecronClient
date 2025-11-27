package cn.boop.necron.config;

public enum ResetReason {
    WORLD_CHANGE("Detection server changed"),
    TELEPORT("Detection position changed"),
    PLAYER_VISIT("Detection guest without whitelist"),
    ITEM_CHANGE("Detection held item changed");

    private final String message;

    ResetReason(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
