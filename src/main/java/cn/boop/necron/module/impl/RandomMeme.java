package cn.boop.necron.module.impl;

import cn.boop.necron.Necron;
import cn.boop.necron.config.SoundManager;
import cn.boop.necron.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static cn.boop.necron.config.impl.NecronOptionsImpl.meme;
import static cn.boop.necron.config.impl.NecronOptionsImpl.memeOdds;

public class RandomMeme {
    private boolean shouldRenderOverlay = false;
    private static long lastTime = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Necron.mc.currentScreen == null || meme) {
            shouldRenderOverlay = SoundManager.checkIfStillPlaying();
        }
    }

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        if (event.buttonstate && meme) {
            if (event.button == 0 || event.button == 1) {
                triggerRandomMeme();
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (meme) {
            triggerRandomMeme();
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.entityLiving instanceof EntityPlayer && meme) {
            triggerRandomMeme();
        }
    }

    /*@SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        if (shouldRenderOverlay) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 10);
            //GLUtils.applyBlackWhiteFilter();
            RenderUtils.drawRoundedRect(0, 0, event.resolution.getScaledWidth(), event.resolution.getScaledHeight(), 0, 0x80808080);
            GlStateManager.popMatrix();
        }
    }*/

    private void triggerRandomMeme() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 5000) {
            if (Math.random() < memeOdds / 100f) {
                int index = Utils.random.nextInt(SoundManager.soundCount);
                float pitch = 0.8f + Utils.random.nextFloat() * 0.8f;
                SoundManager.playSound(index, pitch);
                lastTime = currentTime;
            }
        }
    }
}
