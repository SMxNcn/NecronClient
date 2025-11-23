package cn.boop.necron.command;

import cn.boop.necron.Necron;
import cn.boop.necron.module.ModuleManager;
import cn.boop.necron.module.impl.ctjs.RngMeterManager;
import cn.boop.necron.utils.DungeonUtils;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DebugCommands extends CommandBase {
    private static int banCount = 0;
    private static final String[] DENY_MESSAGES = {"No.", "STOP pls❤", "Alert!"};

    @Override
    public String getCommandName() {
        return "ncdebug";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("ncd");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + this.getCommandName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "ban":
                    banCount++;
                    if (banCount >= 4) {
                        FakeWipe.triggerBanGui();
                        banCount = 0;
                    } else if (banCount == 3) {
                        Necron.mc.thePlayer.addChatComponentMessage(new ChatComponentText("§dFrom §c[§6ዞ§c] §cHypixel§7: You want a ban? Well, well."));
                    } else {
                        Necron.mc.thePlayer.addChatComponentMessage(new ChatComponentText("§c" + DENY_MESSAGES[Utils.random.nextInt(DENY_MESSAGES.length)]));
                    }
                    break;
                case "dungeonInfo":
                    if (!DungeonUtils.dungeonPlayers.isEmpty()) {
                        for (Map.Entry<String, DungeonUtils.DungeonPlayer> entry : DungeonUtils.dungeonPlayers.entrySet()) {
                            Utils.modMessage(String.valueOf(entry.getValue()));
                        }
                    } else {
                        Utils.modMessage("§cNo dungeon players found.");
                    }
                    break;
                case "findpath":
                    if (args.length < 4) {
                        Utils.modMessage("Usage: findpath <x> <y> <z>");
                        break;
                    }

                    try {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                    new Thread(() -> ModuleManager.getAutoPath().setTarget(new BlockPos(x, y, z)));

                    } catch (NumberFormatException e) {
                        Utils.modMessage("§cInvalid position format.");
                    }
                    break;
                case "stats":
                    String itemID, itemName;
                    if (Necron.mc.thePlayer.getHeldItem() != null) {
                        itemID = Utils.getSkyBlockID(Necron.mc.thePlayer.getHeldItem());
                        itemName = Necron.mc.thePlayer.getHeldItem().getDisplayName();
                    } else {
                        itemID = "";
                        itemName = "";
                    }
                    Utils.modMessage("Player Stats:\n§7§l | §r§7inHypixel: " + (LocationUtils.inHypixel ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7inSkyBlock: " + (LocationUtils.inSkyBlock ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7Island: " + LocationUtils.getCurrentIslandName() +
                            "\n§7§l | §r§7Held item ID: " + itemID +
                            "\n§7§l | §r§7Held item Name:§r " + itemName +
                            "\n§7§l | §r§7Player health: §c" + Necron.mc.thePlayer.getHealth() +
                            "\n§7" +
                            "\n§7§l | §r§7inDungeon: " + (LocationUtils.inDungeon ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7Floor: " + LocationUtils.floor +
                            "\n§7§l | §r§7Instance player(s): " + DungeonUtils.dungeonPlayers.size() +
                            "\n§7§l | §r§7Current M7 Phase: " + LocationUtils.getM7Phase()
                            );
                    break;
                case "test":
                    break;
                default:
                    Utils.modMessage("§cUnknown debug command.");
                    break;
            }
        } else {
            Utils.modMessage("Debug Commands: dungeonInfo, findpath, stats");
        }
    }
}
