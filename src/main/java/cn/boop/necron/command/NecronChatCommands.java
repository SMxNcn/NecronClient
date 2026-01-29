package cn.boop.necron.command;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.B64Utils;
import cn.boop.necron.utils.RenderUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NecronChatCommands extends CommandBase {
    private static final Pattern COLOR_PATTERN = Pattern.compile("\\\\&#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})#");

    @Override
    public String getCommandName() {
        return "nchat";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("ncc");
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
        if (args.length == 0) {
            Necron.mc.thePlayer.addChatMessage(new ChatComponentText(helpMsg));
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        String userMessage = messageBuilder.toString();
        String formattedMessage = convertSimplifiedFormat(userMessage).replace("\\&", "§");

        String validationError = validateColorFormat(formattedMessage);
        if (validationError != null) {
            Utils.modMessage(validationError);
            Necron.LOGGER.warn("Blocked invalid color format: {}", formattedMessage);
            return;
        }

        Utils.chatMessage(B64Utils.encodeWithOffset(formattedMessage));
    }

    private String convertSimplifiedFormat(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher colorMatcher = COLOR_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (colorMatcher.find()) {
            String colorValue = colorMatcher.group(1);
            StringBuilder standardFormat = new StringBuilder("\\&#");

            for (char c : colorValue.toCharArray()) {
                standardFormat.append("\\&").append(c);
            }

            standardFormat.append("\\&/");
            colorMatcher.appendReplacement(result, Matcher.quoteReplacement(standardFormat.toString()));
        }

        colorMatcher.appendTail(result);
        return result.toString();
    }

    private String validateColorFormat(String text) {
        if (text == null) return null;

        int startIndex = 0;
        while ((startIndex = text.indexOf("§#", startIndex)) != -1) {
            int endIndex = text.indexOf("§/", startIndex);

            if (endIndex == -1) {
                int hashIndex = text.indexOf('#', startIndex + 2);
                if (hashIndex != -1) {
                    String possibleColor = text.substring(startIndex + 2, hashIndex);
                    if (possibleColor.length() < 6 || possibleColor.length() == 7 || possibleColor.length() > 8) {
                        return "§cInvalid SkyHanni Color: " + possibleColor + " <-[HERE]";
                    }
                }
                startIndex += 2;
                continue;
            }

            String colorSection = text.substring(startIndex + 2, endIndex);
            if (!Necron.isSkyHanni) return "§cCan't resolve color code: SkyHanni is not installed!";
            if (colorSection.isEmpty()) return "§cInvalid SkyHanni Color: null <-[HERE]";
            if (!RenderUtils.isValidSHiColorPattern(colorSection)) return "§cInvalid SkyHanni Color: " + colorSection + " <-[HERE]";
            startIndex = endIndex + 2;
        }

        return null;
    }

    private static final String helpMsg =
            "§8§m-------------------------------------\n" +
            "§b              NecronClient Chat\n" +
            "§r \n" +
            "§b用法： \n" +
            "§b /ncc <message> §f§l»§r§7 发送Base64加密消息\n" +
            "§7 使用 '\\&' 替代Minecraft颜色代码\n" +
            "§7 '\\&#(AA)RRGGBB#' 为SkyHanni的颜色简化格式 (需要安装SkyHanni模组)\n" +
            "§r \n" +
            "§b颜色示例及对应颜色代码：\n" +
            " §bNecron§8Client  §f§l»§r§7  \\&bNecron\\&8Client\n" +
            " §#§8§8§8§e§f§f§/Pastel  §f§l»§r§7  \\&#888eff#Pastel\n" +
            " §#§a§1§e§6§e§5§/Sky  §f§l»§r§7  \\&#\\&a\\&1\\&e\\&6\\&e\\&5\\&/Sky\n" +
            " §#§8§0§6§1§8§4§f§c§/Aurora  §f§l»§r§7  \\&#806184fc#Aurora (50%透明度)\n" +
            "§r§8§m-------------------------------------";
}
