package cn.boop.necron.mixin;

import cn.boop.necron.module.impl.AutoI4;
import cn.boop.necron.module.impl.FailSafe;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S23PacketBlockChange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(method = "handlePlayerPosLook", at = @At("HEAD"))
    public void handlePlayerPosLook(CallbackInfo ci) {
        if (!FailSafe.voidFalling) {
            FailSafe.onPlayerTeleport("Crop Nuker");
        }
    }

    @Inject(method = "handleBlockChange", at = @At("RETURN"))
    public void handleBlockChange(S23PacketBlockChange packet, CallbackInfo ci) {
        AutoI4.INSTANCE.onBlockChangePacket(packet);
    }
}
