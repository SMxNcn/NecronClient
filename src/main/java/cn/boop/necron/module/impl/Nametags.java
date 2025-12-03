package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.regex.Pattern;

import static cn.boop.necron.config.impl.NametagsOptionsImpl.*;

public final class Nametags {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!canDisplayNametags()) return;

        Entity viewer = Necron.mc.getRenderViewEntity();
        if (viewer == null) return;

        float partialTicks = event.partialTicks;
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        for (EntityPlayer entity : Necron.mc.theWorld.playerEntities) {
            if (entity == null || entity == Necron.mc.thePlayer) continue;
            if (!isValidSkyBlockPlayer(entity)) continue;

            double pX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
            double pY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
            double pZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;

            double distance = viewer.getDistance(pX, pY, pZ);
            if (entity.isDead || !entity.isEntityAlive()) continue;
            renderNametag(entity, pX, pY, pZ, (float) distance, partialTicks);
        }
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }

    private void renderNametag(EntityPlayer entity, double x, double y, double z, float distance, float partialTicks) {
        Entity viewer = Necron.mc.getRenderViewEntity();

        float size = Necron.mc.thePlayer.getDistanceToEntity(entity) / 10.0f;
        if (size < 1.1f) size = 1.1f;
        y += entity.isSneaking() ? 0.5 : 0.7;
        float scale = size * 2f / 100.0f;

        GL11.glPushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        GLUtils.backupAndSetupRender();

        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

        GlStateManager.translate(x - viewerX, y - viewerY + entity.getEyeHeight() - 0.1F, z - viewerZ);
        GL11.glRotatef(-Necron.mc.getRenderManager().playerViewY, 0.0f, 2.0f, 0.0f);
        GL11.glRotatef(Necron.mc.getRenderManager().playerViewX, 2.0f, 0.0f, 0.0f);
        GL11.glScalef(-scale, -scale, scale);

        String nametagText = buildNametagText(entity, (int) distance);

        float width = Necron.mc.fontRendererObj.getStringWidth(nametagText);
        float nw = -width / 2f - 4.6f;
        float width2 = nw - 2.0f * nw;

        if (renderBg) {
            RenderUtils.drawRoundedRect(nw, -17.0f, width2, -1.0f, 3.0f, new Color(25, 25, 25, 114).getRGB());
        }
        Necron.mc.fontRendererObj.drawString(nametagText, (int)(nw + 4.0f), -13, 0xFFFFFF, shadowText);

        GlStateManager.depthMask(true);
        GLUtils.restorePreviousRenderState();
        GlStateManager.popMatrix();
        GL11.glPopMatrix();
    }

    private String buildNametagText(EntityPlayer entity, int distance) {
        String playerName = entity.getName();

        if (LocationUtils.inDungeon) {
            String cleanPlayerName = Utils.clearMcUsername(playerName);
            DungeonUtils.DungeonPlayer dungeonPlayer = DungeonUtils.dungeonPlayers.get(cleanPlayerName);

            if (dungeonPlayer != null && dungeonPlayer.getPlayerClass() != null) {
                DungeonUtils.DungeonClass playerClass = dungeonPlayer.getPlayerClass();
                String classInitial = playerClass.getClassName().substring(0, 1);
                String classColor = DungeonUtils.getPlayerClassColor(playerName);

                return classColor + "[" + classInitial + "] " + playerName;
            } else {
                return "§7[?] " + playerName;
            }
        } else {
            String displayName = entity.getDisplayName().getFormattedText();
            return displayName + (renderDistance ? " §7" + distance + "m§r" : "§r");
        }
    }

    @SubscribeEvent
    public void onRenderName(RenderLivingEvent.Specials.Pre<EntityPlayer> event) {
        if (event.entity instanceof EntityPlayer && event.entity != Necron.mc.thePlayer) {
            if (canDisplayNametags()) event.setCanceled(true);
        }
    }

    private boolean canDisplayNametags() {
        return nametags && (forceSkyBlock || LocationUtils.inSkyBlock);
    }

    private boolean isValidSkyBlockPlayer(EntityPlayer entity) {
        String displayName = entity.getDisplayName().getFormattedText();
        String cleanName = StringUtils.stripControlCodes(displayName);
        if (forceSkyBlock) return (!cleanName.contains("[NPC]") || !cleanName.contains("CIT-"));
        return Pattern.matches("^\\[\\d{1,3}]\\s[a-zA-Z0-9_]{1,16}.*", cleanName);
    }
}
