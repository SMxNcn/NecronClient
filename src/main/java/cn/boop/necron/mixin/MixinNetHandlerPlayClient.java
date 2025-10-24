package cn.boop.necron.mixin;

import cn.boop.necron.module.impl.FailSafe;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(method = "handlePlayerPosLook", at = @At("HEAD"))
    public void handlePlayerPosLook(CallbackInfo ci) {
        FailSafe.onPlayerTeleport("Crop Nuker");
    }
}
