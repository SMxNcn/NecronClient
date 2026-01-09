package cn.boop.necron.command;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.*;
import cn.boop.necron.module.impl.item.ItemProtector;
import cn.boop.necron.utils.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;

public class DebugCommands extends CommandBase {
    private static int banCount = 0;
    private static final String[] DENY_MESSAGES = {"No.", "STOP pls❤", "Alert!"};
    private final List<String> commands = Arrays.asList("ban", "dungeonInfo", "findpath", "i4", "setIndex", "stats", "stoppath", "update");

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
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, commands);
        }
        return new ArrayList<>();
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
                    if (DungeonUtils.dungeonPlayers.isEmpty()) {
                        Utils.modMessage("§cNo dungeon players found.");
                        return;
                    }
                    StringBuilder message = new StringBuilder("dungeonInfo: [");
                    boolean first = true;
                    for (Map.Entry<String, DungeonUtils.DungeonPlayer> entry : DungeonUtils.dungeonPlayers.entrySet()) {
                        if (!first) {
                            message.append(", ");
                        }
                        DungeonUtils.DungeonPlayer player = entry.getValue();
                        message.append("{name='").append(player.getPlayerName())
                                .append("', class='").append(player.getPlayerClass())
                                .append("', level=").append(player.getClassLevel()).append("}");
                        first = false;
                    }
                    message.append("]");
                    Utils.modMessage(message.toString());
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
                        new Thread(() -> {
                            AutoPath.startNavigation(new BlockPos(x, y, z));
                            Utils.modMessage("Teleport pathfinder start.");
                        }).start();

                    } catch (NumberFormatException e) {
                        Utils.modMessage("§cInvalid position format.");
                    }
                    break;
                case "nbt":
                    ItemStack held = Necron.mc.thePlayer.getHeldItem();
                    if (held == null || held.getItem() == null) return;
                    NBTTagCompound nbt = new NBTTagCompound();
                    held.writeToNBT(nbt);

                    String nbtString = nbt.toString();
                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(new StringSelection(nbtString), null);
                    Utils.modMessage("§aNBT data copied to clipboard!");
                    break;
                case "i4":
                    AutoI4.INSTANCE.reset();
                    break;
                case "setIndex":
                    CropNuker.setIndex(Integer.parseInt(args[1]));
                    Utils.modMessage("Set index to " + args[1]);
                    break;
                case "stats":
                    String itemID, itemName, itemUUID;
                    if (Necron.mc.thePlayer.getHeldItem() != null) {
                        itemID = ItemUtils.getSkyBlockID(Necron.mc.thePlayer.getHeldItem());
                        itemName = Necron.mc.thePlayer.getHeldItem().getDisplayName();
                        itemUUID = ItemUtils.getItemUUID(Necron.mc.thePlayer.getHeldItem());
                    } else {
                        itemID = "";
                        itemName = "";
                        itemUUID = "";
                    }
                    Utils.modMessage("Player Stats:\n§7§l | §r§7inHypixel: " + (LocationUtils.inHypixel ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7inSkyBlock: " + (LocationUtils.inSkyBlock ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7Island: " + LocationUtils.getCurrentIslandName() +
                            "\n§7§l | §r§7Zone: " + LocationUtils.currentZone +
                            "\n§7§l | §r§7Held item ID: " + itemID +
                            "\n§7§l | §r§7Held item Name:§r " + itemName +
                            "\n§7§l | §r§7Held item UUID: " + itemUUID +
                            "\n§7§l | §r§7Item protected: " + (ItemProtector.isItemProtected(Necron.mc.thePlayer.getHeldItem()) ? "§atrue" : "§cfalse") +
                            "\n§7" +
                            "\n§7§l | §r§7inDungeon: " + (LocationUtils.inDungeon ? "§atrue" : "§cfalse") +
                            "\n§7§l | §r§7Floor: " + LocationUtils.floor +
                            "\n§7§l | §r§7Instance player(s): " + DungeonUtils.dungeonPlayers.size() +
                            "\n§7§l | §r§7Current M7 Phase: " + LocationUtils.getM7Phase() +
                            "\n§7§l | §r§7Current P3 Stage: " + LocationUtils.getP3Stage()
                    );
                    break;
                case "stoppath":
                    AutoPath.stopNavigation();
                    Utils.modMessage("Teleport pathfinder stop.");
                    break;
                case "test":
//                    List<String> scoreboard = ScoreboardUtils.getScoreboard();
//                    for (String line : scoreboard) {
//                       // lines[scoreboard.indexOf(line)] = ScoreboardUtils.cleanSB(line) + "\n";
//                        System.out.println(line);
//                    }
                    new Thread(() -> {
                        try {
                            if (AutoLeap.isLeapItem(Necron.mc.thePlayer.getHeldItem())) PlayerUtils.rightClick();
                            else Utils.modMessage("§cYou are not holding a Leap!");
                            Thread.sleep(600);
                            AutoLeap.leapToPlayer(args[1]);
                            //AutoLeap.leapToClass(DungeonUtils.DungeonClass.fromName(args[1]));
                        } catch (Exception e) {
                            Necron.LOGGER.error("Error while executing /test: ", e);
                        }
                    }).start();
                    //Utils.chatMessage(B64Utils.encodeWithOffset(FakeWipe.triggerMsg));
                    break;
                case "update":
                    DungeonUtils.INSTANCE.updateDungeonPlayers();
                    break;
                default:
                    Utils.modMessage("§cUnknown debug command.");
                    break;
            }
        } else {
            Utils.modMessage("Debug Commands: ban, dungeonInfo, findpath, i4, setIndex, stats, stoppath, update");
        }
    }
}
