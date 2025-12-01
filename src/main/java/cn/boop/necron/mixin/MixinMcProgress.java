package cn.boop.necron.mixin;

import cn.boop.necron.gui.LoadingScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin({Minecraft.class})
public class MixinMcProgress {
    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;textureMapBlocks:Lnet/minecraft/client/renderer/texture/TextureMap;", shift = At.Shift.AFTER)})
    private void textureMapBlocks(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;modelManager:Lnet/minecraft/client/resources/model/ModelManager;", shift = At.Shift.AFTER)})
    private void modelManager(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;renderManager:Lnet/minecraft/client/renderer/entity/RenderManager;", shift = At.Shift.AFTER, ordinal = 0)})
    private void renderManager(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;entityRenderer:Lnet/minecraft/client/renderer/EntityRenderer;", shift = At.Shift.AFTER)})
    private void entityRenderer(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;blockRenderDispatcher:Lnet/minecraft/client/renderer/BlockRendererDispatcher;", shift = At.Shift.AFTER)})
    private void blockRenderDispatcher(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;renderGlobal:Lnet/minecraft/client/renderer/RenderGlobal;", shift = At.Shift.AFTER)})
    private void renderGlobal(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;itemRenderer:Lnet/minecraft/client/renderer/ItemRenderer;", shift = At.Shift.AFTER, ordinal = 0)})
    private void itemRenderer(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;guiAchievement:Lnet/minecraft/client/gui/achievement/GuiAchievement;", shift = At.Shift.AFTER, ordinal = 0)})
    private void guiAchievement(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;effectRenderer:Lnet/minecraft/client/particle/EffectRenderer;", shift = At.Shift.AFTER, ordinal = 0)})
    private void effectRenderer(CallbackInfo ci) {
        incrementProgress();
    }

    @Inject(method = {"startGame"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;renderItem:Lnet/minecraft/client/renderer/entity/RenderItem;", shift = At.Shift.AFTER)})
    private void renderItem(CallbackInfo ci) {
        incrementProgress();
    }

    private static void incrementProgress() {
        if (LoadingScreen.progress < LoadingScreen.MAX_PROGRESS) {
            ++LoadingScreen.progress;
        }
    }
}
