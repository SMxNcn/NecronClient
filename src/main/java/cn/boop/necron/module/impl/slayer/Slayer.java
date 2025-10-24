package cn.boop.necron.module.impl.slayer;

public enum Slayer {
    Unknown("Unknown"),
    Revenant("Zombie Slayer"),
    Tarantula("Spider Slayer"),
    Sven("Wolf Slayer"),
    Voidgloom("Enderman Slayer"),
    Riftstalker("Vampire Slayer"),
    Inferno("Blaze Slayer");

    private final String displayName;

    Slayer(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
