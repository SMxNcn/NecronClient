package cn.boop.necron.module.impl.hud;

import cn.boop.necron.utils.RenderUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collection;
import java.util.List;

import static cn.boop.necron.config.impl.GUIOptionsImpl.*;

public class CustomScoreboard {
    public static final CustomScoreboard INSTANCE = new CustomScoreboard();

    public void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, FontRenderer fontObj) {
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")));

        if (list.size() > 15) collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        else collection = list;

        int displayNameWidth = fontObj.getStringWidth(objective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());
            String fullString = playerName;

            if (redNumbers) {
                String scoreString = ": " + EnumChatFormatting.RED + score.getScorePoints();
                fullString = playerName + scoreString;
            }

            displayNameWidth = Math.max(displayNameWidth, fontObj.getStringWidth(fullString));
        }

        int totalHeight = collection.size() * fontObj.FONT_HEIGHT;
        int startY = scaledRes.getScaledHeight() / 2 + totalHeight / 3;
        int rightMargin = xPadding + 5;
        int leftStart = scaledRes.getScaledWidth() - displayNameWidth - rightMargin - (redNumbers ? xPadding : 0);

        int bgLeft = leftStart - 2 - (redNumbers ? xPadding : 0);
        int bgTop = startY - collection.size() * fontObj.FONT_HEIGHT - fontObj.FONT_HEIGHT - 5 - yPadding;
        int bgRight = scaledRes.getScaledWidth() - rightMargin + 2 + xPadding;
        int bgBottom = startY + yPadding;

        RenderUtils.drawRoundedRect(bgLeft, bgTop, bgRight, bgBottom, sbCornerRadius, sbBgColor.toJavaColor().getRGB());

        int currentIndex = 0;
        for(Score score : collection) {
            currentIndex++;
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score.getPlayerName());

            int textY = startY - currentIndex * fontObj.FONT_HEIGHT;
            int rightEdge = scaledRes.getScaledWidth() - rightMargin + 2;
            int textRightMargin = redNumbers ? (xPadding + 5) : (xPadding + 2);
            int textLeftStart = scaledRes.getScaledWidth() - displayNameWidth - textRightMargin - (redNumbers ? xPadding : 1);

            if (currentIndex == 1 && !customIp.isEmpty()) continue;

            fontObj.drawString(playerName, textLeftStart, textY, 553648127, shadowText);
            if (redNumbers) {
                String scoreValue = EnumChatFormatting.RED + "" + score.getScorePoints();
                fontObj.drawString(scoreValue, rightEdge - fontObj.getStringWidth(scoreValue) - xPadding + 2, textY, 553648127, shadowText);
            }

            if (currentIndex == collection.size()) {
                String displayName = objective.getDisplayName();
                int titleRightMargin = redNumbers ? (xPadding + 5) : (xPadding + 2);
                int titleLeftStart = scaledRes.getScaledWidth() - displayNameWidth - titleRightMargin - (redNumbers ? xPadding : 0);
                int titleX = titleLeftStart + (redNumbers ? xPadding : -1) + displayNameWidth / 2 - fontObj.getStringWidth(displayName) / 2;
                int titleY = textY - fontObj.FONT_HEIGHT - yPadding;
                fontObj.drawString(displayName, titleX, titleY, 553648127, shadowText);
            }
        }

        if (!customIp.isEmpty() && !collection.isEmpty()) {
            String formattedIp = customIp.replace('&', '§');

            int lastIndex = 1;
            int ipY = startY - lastIndex * fontObj.FONT_HEIGHT;
            int ipRightMargin = redNumbers ? (xPadding + 5) : (xPadding + 2);
            int ipLeftStart = scaledRes.getScaledWidth() - displayNameWidth - ipRightMargin - (redNumbers ? xPadding : 1);

            int ipX = alignCenter ? ipLeftStart + displayNameWidth / 2 - fontObj.getStringWidth(clientName ? "Necron Client" : formattedIp) / 2 : ipLeftStart;
            fontObj.drawString(
                    clientName ? "Necron Client" : formattedIp,
                    ipX, ipY,
                    clientName ? RenderUtils.getChromaColor(startColor.toJavaColor(), endColor.toJavaColor(), 6, chromaSpeed, colorOffset).getRGB() : 553648127,
                    shadowText
            );
        }
    }
}
