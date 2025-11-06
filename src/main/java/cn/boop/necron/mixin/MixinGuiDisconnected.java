package cn.boop.necron.mixin;

import cn.boop.necron.utils.ServerUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public abstract class MixinGuiDisconnected extends GuiScreen {
    @Shadow
    private int field_175353_i;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInitGui(CallbackInfo ci) {
        ServerData currentServerData = ServerUtils.lastServerData;
        if (currentServerData != null) {
            GuiButton reconnectButton = new GuiButton(
                    91,
                    this.width / 2 - 100,
                    this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 24,
                    200,
                    20,
                    I18n.format("client.gui.reconnect"));
            this.buttonList.add(reconnectButton);
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 91) {
            ServerUtils.reconnectToServer();
        }
    }
}
