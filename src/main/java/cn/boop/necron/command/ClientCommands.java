package cn.boop.necron.command;

import cc.polyfrost.oneconfig.utils.gui.GuiUtils;
import cn.boop.necron.Necron;
import cn.boop.necron.gui.GuiWaypointList;
import cn.boop.necron.module.impl.ChatBlocker;
import cn.boop.necron.module.impl.ChatCommands;
import cn.boop.necron.module.impl.Waypoint;
import cn.boop.necron.utils.B64Utils;
import cn.boop.necron.utils.RotationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientCommands extends CommandBase {
    private final List<String> commands = Arrays.asList("b64", "create", "rotate", "tips", "wl", "wp");

    @Override
    public String getCommandName() {
        return "necron";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("nc");
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
                case "b64":
                    if (args.length < 2) {
                        Utils.modMessage("Usage: b64 <message>");
                        break;
                    }
                    StringBuilder messageBuilder = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        if (i > 1) messageBuilder.append(" ");
                        messageBuilder.append(args[i]);
                    }
                    String message = messageBuilder.toString();

                    String encoded = B64Utils.encodeWithOffset(message);
                    Utils.chatMessage(encoded);
                    break;
                case "create":
                    if (args.length < 2) {
                        Utils.modMessage("Usage: create <fileName>");
                        break;
                    }
                    Waypoint.loadWaypoints(args[1], false);
                    break;
                case "rotate":
                    if (args.length < 4) {
                        Utils.modMessage("Usage: rotate <x> <y> <z>");
                        break;
                    }
                    try {
                        double x = Double.parseDouble(args[1]);
                        double y = Double.parseDouble(args[2]);
                        double z = Double.parseDouble(args[3]);
                        RotationUtils.rotatingToBlock(x, y, z, 0.3f);
                        Utils.modMessage(String.format("Rotating to Vec3d: (%.1f, %.1f, %.1f)", x, y, z));
                    } catch (NumberFormatException e) {
                        Utils.modMessage("§cInvalid position format.");
                    }
                    break;
                case "tips":
                    Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§bTips §8»§r§7 " + Utils.randomSelect(ChatCommands.tipList)));
                    break;
                case "wl":
                    handleWhitelistCommand(Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "wp":
                    GuiUtils.displayScreen(new GuiWaypointList());
                    break;
                default:
                    Utils.modMessage("§cUnknown command.");
                    break;
            }
        } else {
            Necron.mc.thePlayer.addChatMessage(new ChatComponentText(helpMsg));
        }
    }

    private void handleWhitelistCommand(String[] args) {
        if (args.length < 1) {
            Utils.modMessage("Usage: wl <add | remove> [player]");
            return;
        }

        switch (args[0]) {
            case "add":
                if (args.length < 2) {
                    Utils.modMessage("Usage: wl add <player>");
                    return;
                }
                if (ChatBlocker.addToWhitelist(args[1])) {
                    Utils.modMessage("Added §a" + args[1] + "§7 to whitelist.");
                } else {
                    Utils.modMessage("§a" + args[1] + "§7 is already in whitelist.");
                }
                break;
            case "remove":
                if (args.length < 2) {
                    Utils.modMessage("Usage: wl remove <player>");
                    return;
                }
                if (ChatBlocker.removeFromWhitelist(args[1])) {
                    Utils.modMessage("Removed §a" + args[1] + "§7 from whitelist.");
                } else {
                    Utils.modMessage("§a" + args[1] + "§7 is not in whitelist.");
                }
                break;
            default:
                Utils.modMessage("Usage: wl <add | remove> [player]");
                break;
        }
    }

    private static final String helpMsg =
            "§8§m-------------------------------------\n" +
            "§b             NecronClient §7v" + Necron.VERSION + "\n" +
            "§r \n" +
            "§b/necron b64 <message> §f§l»§r§7 发送Base64加密消息\n" +
            "§b/necron create <fileName> §f§l»§r§7 在目录下新建路径点\n" +
            "§b/necron rotate <x> <y> <z> §f§l»§r§7 将视角旋转至x, y, z\n" +
            "§b/necron tips §f§l»§r§7 获取一些神秘文本\n" +
            "§b/necron wp §f§l»§r§7 打开Waypoint GUI\n" +
            "§b/necron wl <add | remove> [player] §f§l»§r§7 管理白名单\n" +
            "§r§8§m-------------------------------------";
}