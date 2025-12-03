package cn.boop.necron.mixin;

import cn.boop.necron.utils.RenderUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Collection;
import java.util.List;

import static cn.boop.necron.config.impl.GUIOptionsImpl.*;
import static cn.boop.necron.config.impl.ScrollingOptionsImpl.hotbarSmoothness;
import static cn.boop.necron.config.impl.ScrollingOptionsImpl.smoothHotbarSc;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Unique private int rolloverOffset = 4;
    @Unique private float selectedPixelBuffer = 0;
    @Unique private boolean masked = false;
    @Unique private int lastSelectedSlot = 0;
    @Unique private int effectiveRollover = 0;

    @Unique private int storedM;
    @Unique private int storedL;
    @Unique private int storedFirstP;
    @Unique private int storedLastP;
    @Unique private int storedFontHeight;
    @Unique private int storedCollectionSize;
    @Unique private int storedCurrentIndex = 0;
    @Unique private boolean shouldDrawCompleteBackground = false;
    @Unique private FontRenderer fontObj = Minecraft.getMinecraft().fontRendererObj;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void injectBackground(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
        if (!customSb) return;
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")));

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

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

        RenderUtils.drawRoundedRect(bgLeft, bgTop, bgRight, bgBottom, cornerRadius, bgColor.toJavaColor().getRGB());

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

            fontObj.drawString(playerName, textLeftStart, textY, 553648127);
            if (redNumbers) {
                String scoreValue = EnumChatFormatting.RED + "" + score.getScorePoints();
                fontObj.drawString(scoreValue, rightEdge - fontObj.getStringWidth(scoreValue) - xPadding + 2, textY, 553648127);
            }

            if (currentIndex == collection.size()) {
                String displayName = objective.getDisplayName();
                int titleRightMargin = redNumbers ? (xPadding + 5) : (xPadding + 2);
                int titleLeftStart = scaledRes.getScaledWidth() - displayNameWidth - titleRightMargin - (redNumbers ? xPadding : 0);
                int titleX = titleLeftStart + (redNumbers ? xPadding : -1) + displayNameWidth / 2 - fontObj.getStringWidth(displayName) / 2;
                int titleY = textY - fontObj.FONT_HEIGHT - yPadding;
                fontObj.drawString(displayName, titleX, titleY, 553648127);
            }
        }

        if (!customIp.isEmpty() && !collection.isEmpty()) {
            String formattedIp = customIp.replace('&', '§');

            int lastIndex = 1;
            int ipY = startY - lastIndex * fontObj.FONT_HEIGHT;
            int ipRightMargin = redNumbers ? (xPadding + 5) : (xPadding + 2);
            int ipLeftStart = scaledRes.getScaledWidth() - displayNameWidth - ipRightMargin - (redNumbers ? xPadding : 1);

            int ipX = ipLeftStart + displayNameWidth / 2 - fontObj.getStringWidth(formattedIp) / 2;

            fontObj.drawString(formattedIp, ipX, ipY, 553648127, false);
        }

        ci.cancel();
    }

    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1))
    public void selectedSlotX(Args args, ScaledResolution res, float partialTicks) {
        if (hotbarSmoothness <= 0 || !smoothHotbarSc) return;

        int x = args.get(0);
        int y = args.get(1);
        int textureX = args.get(2);
        int textureY = args.get(3);
        int width = args.get(4);
        int height = args.get(5);

        Minecraft mc = Minecraft.getMinecraft();
        InventoryPlayer inv = mc.thePlayer.inventory;
        int currentSlot = inv.currentItem;

        if (currentSlot != lastSelectedSlot) {
            if ((lastSelectedSlot == 8 && currentSlot == 0) || (lastSelectedSlot == 1 && currentSlot == 8)) {
                effectiveRollover -= 1;
            } else if ((lastSelectedSlot == 7 && currentSlot == 0) || (lastSelectedSlot == 0 && currentSlot == 8)) {
                effectiveRollover += 1;
            } else {
                int actualPosition = currentSlot * 20;
                if (Math.abs(selectedPixelBuffer - actualPosition) > 160) {
                    selectedPixelBuffer = actualPosition;
                    effectiveRollover = 0;
                }
            }
            lastSelectedSlot = currentSlot;
        }

        int target = (inv.currentItem - effectiveRollover * 9) * 20 - effectiveRollover * rolloverOffset;
        float animationSpeed = Math.min(Math.max(0.01f, hotbarSmoothness * 0.1f), 0.9f);
        float delta = getLastFrameDuration();
        selectedPixelBuffer = (float) ((selectedPixelBuffer - target) * Math.pow(animationSpeed, delta) + target);

        if (Math.round(selectedPixelBuffer) < -10 - rolloverOffset) {
            selectedPixelBuffer += 9 * 20 + rolloverOffset;
            effectiveRollover -= 1;
        } else if (Math.round(selectedPixelBuffer) > 20 * 9 - 10 + rolloverOffset) {
            selectedPixelBuffer -= 9 * 20 + rolloverOffset;
            effectiveRollover += 1;
        }

        x -= inv.currentItem * 20;
        x += Math.round(selectedPixelBuffer);
        args.set(0, x);

        masked = false;
        if (Math.round(selectedPixelBuffer) < 0) {
            enableScissor(res);
            drawHotbarRolloverMirror(x, 9 * 20, rolloverOffset, y, textureX, textureY, width, height);
        } else if (Math.round(selectedPixelBuffer) > 20 * 8) {
            enableScissor(res);
            drawHotbarRolloverMirror(x, -9 * 20, -rolloverOffset, y, textureX, textureY, width, height);
        }
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void afterSelectedSlot(ScaledResolution res, float partialTicks, CallbackInfo ci) {
        if (masked) disableScissor();
    }

    @Unique
    private void enableScissor(ScaledResolution res) {
        int x = res.getScaledWidth() / 2 - 91;
        int y = res.getScaledHeight() - 22;
        int scale = res.getScaleFactor();

        int windowHeight = Minecraft.getMinecraft().displayHeight;
        int scissorY = windowHeight - (y + 22) * scale;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, scissorY, 182 * scale, 22 * scale);
        masked = true;
    }

    @Unique
    private void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        masked = false;
    }

    @Unique
    private void drawHotbarRolloverMirror(int x, int hotbarWidth, int offset, int y, int textureX, int textureY, int width, int height) {
        GuiIngame gui = (GuiIngame) (Object) this;
        gui.drawTexturedModalRect(x + hotbarWidth + offset, y, textureX, textureY, width, height);
    }

    @Unique
    private static float getLastFrameDuration() {
        Minecraft mc = Minecraft.getMinecraft();
        Timer timer = ((MinecraftAccessor) mc).getTimer();
        return timer.elapsedPartialTicks;
    }
}