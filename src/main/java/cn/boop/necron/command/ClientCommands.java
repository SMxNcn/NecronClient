package cn.boop.necron.command;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.ChatCommands;
import cn.boop.necron.module.impl.CropNuker;
import cn.boop.necron.module.impl.EtherwarpRouter;
import cn.boop.necron.module.impl.Waypoint;
import cn.boop.necron.utils.B64Utils;
import cn.boop.necron.utils.RotationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class ClientCommands extends CommandBase {
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
                    Utils.chatMessage("/ac " + encoded);
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
                case "setDir":
                    if (args.length < 3) {
                        Utils.modMessage("Usage: setDir <waypointID> <direction>");
                        Utils.modMessage("Available directions: forward, backward, left, right.");
                        break;
                    }
                    try {
                        int waypointId = Integer.parseInt(args[1]);
                        String direction = args[2].toLowerCase();

                        if (!Arrays.asList("forward", "backward", "left", "right").contains(direction)) {
                            Utils.modMessage("§cInvalid direction.");
                            Utils.modMessage("Available directions: forward, backward, left, right.");
                            break;
                        }

                        Waypoint.setWaypointDirection(waypointId, direction);
                    } catch (NumberFormatException e) {
                        Utils.modMessage("§cInvalid waypoint ID format.");
                    }
                    break;
                case "setRot":
                    if (args.length < 3) {
                        Utils.modMessage("Usage: setRot <waypointID> <yaw>");
                        Utils.modMessage("The range of the yaw must be in 0~360.");
                        break;
                    }
                    try {
                        int waypointId = Integer.parseInt(args[1]);
                        float rotation = Float.parseFloat(args[2]);

                        rotation = rotation % 360;
                        if (rotation < 0) rotation += 360;

                        Waypoint.setWaypointRotation(waypointId, rotation);
                    } catch (NumberFormatException e) {
                        Utils.modMessage("§cInvalid number format.");
                    }
                    break;
                case "tips":
                    Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§bTips §8»§r§7 " + Utils.randomSelect(ChatCommands.tipList)));
                    break;
                case "load":
                    if (args.length < 2) {
                        Utils.modMessage("Usage: load <file>");
                        break;
                    }
                    EtherwarpRouter.loadWaypoints(args[1]);
                    Waypoint.loadWaypoints(args[1]);
                    CropNuker.setIndex(0);
                    break;
                default:
                    Utils.modMessage("§cUnknown command.");
                    break;
            }
        } else {
            Necron.mc.thePlayer.addChatMessage(new ChatComponentText(helpMsg));
        }
    }

    private static final String helpMsg =
            "§8§m-------------------------------------\n" +
            "§b             NecronClient §7v" + Necron.VERSION + "\n" +
            "§r \n" +
            "§b/necron load <file> §f§l»§r§7 加载路径点文件\n" +
            "§b/necron rotate <x> <y> <z> §f§l»§r§7 将视角旋转至x, y, z\n" +
            "§b/necron setDir <ID> <direction> §f§l»§r§7 设置路径点的移动方向\n" +
            "§b/necron setRot <ID> <yaw> §f§l»§r§7 设置路径点的预设旋转角度\n" +
            "§b/necron tips §f§l»§r§7 获取一些神秘文本\n" +
            "§r§8§m-------------------------------------";
}
