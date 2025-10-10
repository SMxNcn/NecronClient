package cn.boop.necron.mixin;

import cn.boop.necron.utils.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting {
    @Inject(method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ServerData;)V", at = @At("RETURN"))
    private void onInit(GuiScreen parentScreen, Minecraft mcIn, ServerData serverDataIn, CallbackInfo ci) {
        if (serverDataIn != null) {
            ServerUtils.lastServerData = (serverDataIn);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V", at = @At("RETURN"))
    private void onInit(GuiScreen parentScreen, Minecraft mcIn, String host, int port, CallbackInfo ci) {
        if (host != null && !host.isEmpty()) {
            ServerUtils.lastServerData = new ServerData("Direct Connect", host + ":" + port, false);
        }
    }
}
