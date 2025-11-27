package cn.boop.necron.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static cn.boop.necron.config.impl.ScrollingOptionsImpl.hotbarSmoothness;
import static cn.boop.necron.config.impl.ScrollingOptionsImpl.smoothHotbarSc;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Unique private int rolloverOffset = 4;
    @Unique private float selectedPixelBuffer = 0;
    @Unique private boolean masked = false;
    @Unique private int lastSelectedSlot = 0;
    @Unique private int effectiveRollover = 0;

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
            if (lastSelectedSlot == 8 && currentSlot == 0) {
                effectiveRollover -= 1;
            } else if (lastSelectedSlot == 0 && currentSlot == 8) {
                effectiveRollover += 1;
            } else {
                int actualPosition = currentSlot * 20;
                if (Math.abs(selectedPixelBuffer - actualPosition) > 140) {
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