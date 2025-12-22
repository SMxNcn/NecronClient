package cn.boop.necron.module.impl;

import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.DungeonOptionsImpl.*;

public class MaskNotifier {
    private static final Pattern BONZO_PATTERN = Pattern.compile("^Your (?:. )?Bonzo's Mask saved your life!$");
    private static final Pattern SPIRIT_PATTERN = Pattern.compile("^Second Wind Activated! Your Spirit Mask saved your life!$");
    private static final Pattern PHOENIX_PATTERN = Pattern.compile("^Your Phoenix Pet saved you from certain death!$");

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!maskNotifier || !LocationUtils.inSkyBlock) return;

        Matcher bonzoMatcher = BONZO_PATTERN.matcher(event.message.getUnformattedText());
        Matcher spiritMatcher = SPIRIT_PATTERN.matcher(event.message.getUnformattedText());
        Matcher phoenixMatcher = PHOENIX_PATTERN.matcher(event.message.getUnformattedText());

        if (bonzoMatcher.matches() && !bonzoText.isEmpty()) Utils.chatMessage("/pc " + bonzoText);
        if (spiritMatcher.matches() && !bonzoText.isEmpty()) Utils.chatMessage("/pc " + spiritText);
        if (phoenixMatcher.matches() && !bonzoText.isEmpty()) Utils.chatMessage("/pc " + phoenixText);
    }
}
