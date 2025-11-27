package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.JsonUtils;
import cn.boop.necron.utils.LocationUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.ChatBlockerOptionsImpl.chatBlocker;
import static cn.boop.necron.config.impl.ChatBlockerOptionsImpl.whitelistEnabled;

public class ChatBlocker {
    public static final List<String> whitelist = JsonUtils.loadWhitelist();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!chatBlocker && !LocationUtils.inSkyBlock) return;
        
        String msg = event.message.getUnformattedText();
        String formattedMsg = msg.toLowerCase();

        if (whitelistEnabled && isWhitelisted(msg)) {
            return;
        }

        if (Pattern.matches("^(\\[\\d+]|\\[\\D[^]]*]|[^\\[]).*", msg) && !msg.contains(Necron.mc.thePlayer.getName())) {
            if (formattedMsg.contains("map")
                    || formattedMsg.contains("my ah")
                    || formattedMsg.contains("visit ")
                    || formattedMsg.contains("lowbal")
                    || formattedMsg.contains("lb")
                    || formattedMsg.contains("free")
                    || formattedMsg.contains("buy")
                    || formattedMsg.contains("sell")
                    || formattedMsg.contains("discord")) {
                event.setCanceled(true);
            }
        } else {
            if (formattedMsg.contains("this ability is on cooldown for") || formattedMsg.contains("there are blocks in the way!")) {
                event.setCanceled(true);
            }
        }
    }

    public static boolean addToWhitelist(String player) {
        String normalizedPlayer = player.toLowerCase();
        if (!containsIgnoreCase(normalizedPlayer)) {
            whitelist.add(normalizedPlayer);
            JsonUtils.saveWhitelist(whitelist);
            return true;
        }
        return false;
    }

    public static boolean removeFromWhitelist(String player) {
        String normalizedPlayer = player.toLowerCase();
        boolean removed = whitelist.removeIf(item -> item.equalsIgnoreCase(normalizedPlayer));
        if (removed) {
            JsonUtils.saveWhitelist(whitelist);
            return true;
        }
        return false;
    }

    private boolean isWhitelisted(String message) {
        for (String player : whitelist) {
            if (Pattern.matches("(?i)\\[\\d+]\\s*" + Pattern.quote(player) + ".*", message)) {
                return true;
            }

            if (Pattern.matches("(?i)\\[\\D[^]]*]\\s*" + Pattern.quote(player) + ".*", message)) {
                return true;
            }

            if (Pattern.matches("(?i)" + Pattern.quote(player) + ".*", message)) {
                return true;
            }

            if (message.toLowerCase().contains("§7" + player.toLowerCase())) {
                return true;
            }

            if (Pattern.matches("(?i)(Party|Guild|Co-op) > \\[?[\\w+]*]?\\s*" + Pattern.quote(player) + ":.*", message) ||
                Pattern.matches("(?i)From \\[?[\\w+]*]?\\s*" + Pattern.quote(player) + ":.*", message)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPlayerWhitelisted(String playerName) {
        return whitelist.stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
    }

    private static boolean containsIgnoreCase(String str) {
        for (String item : ChatBlocker.whitelist) {
            if (item.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }
}