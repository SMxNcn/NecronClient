package cn.boop.necron.module.impl.item;

public enum GuiType {
    AUCTION("§cCannot create auction on protected item! (%s§c)"),
    DROP("§cCannot drop protected item! (%s§c)"),
    SALVAGE("§cCannot salvage protected item! (%s§c)"),
    SELL("§cCannot sell protected item! (%s§c)"),
    UNKNOWN("§cProtected item! (%s§c)");

    private final String messageFormat;

    GuiType(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String getMessageFormat(String itemName) {
        return String.format(this.messageFormat, itemName);
    }
}
