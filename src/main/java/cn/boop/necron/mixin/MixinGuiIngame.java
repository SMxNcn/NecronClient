package cn.boop.necron.mixin;

import cn.boop.necron.module.impl.hud.CustomScoreboard;
import cn.boop.necron.module.impl.smoothscroll.SmoothHotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static cn.boop.necron.config.impl.GUIOptionsImpl.customSb;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Unique private FontRenderer fontObj = Minecraft.getMinecraft().fontRendererObj;
    @Unique private final SmoothHotbar smoothHotbar = SmoothHotbar.INSTANCE;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void injectBackground(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
        if (!customSb) return;
        CustomScoreboard.INSTANCE.renderScoreboard(objective, scaledRes, fontObj);
        ci.cancel();
    }

    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1))
    public void selectedSlotX(Args args, ScaledResolution res, float partialTicks) {
        int x = args.get(0);
        int y = args.get(1);
        int textureX = args.get(2);
        int textureY = args.get(3);
        int width = args.get(4);
        int height = args.get(5);

        x = smoothHotbar.calculateHotbarX(x);
        args.set(0, x);

        if (smoothHotbar.needsRolloverHandling()) {
            smoothHotbar.enableScissor(res);

            if (smoothHotbar.isRolloverNegative()) {
                drawHotbarRolloverMirror(x, smoothHotbar.getRolloverXAdjustment(),
                        smoothHotbar.getRolloverOffset(), y, textureX, textureY, width, height);
            } else if (smoothHotbar.shouldNegateOffset()) {
                drawHotbarRolloverMirror(x, -smoothHotbar.getRolloverXAdjustment(),
                        -smoothHotbar.getRolloverOffset(), y, textureX, textureY, width, height);
            }
        }
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void afterSelectedSlot(ScaledResolution res, float partialTicks, CallbackInfo ci) {
        if (smoothHotbar.isMasked()) {
            smoothHotbar.disableScissor();
        }
    }

    @Unique
    private void drawHotbarRolloverMirror(int x, int hotbarWidth, int offset, int y, int textureX, int textureY, int width, int height) {
        GuiIngame gui = (GuiIngame) (Object) this;
        gui.drawTexturedModalRect(x + hotbarWidth + offset, y, textureX, textureY, width, height);
    }
}