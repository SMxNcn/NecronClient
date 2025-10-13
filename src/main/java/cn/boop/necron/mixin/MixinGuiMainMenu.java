package cn.boop.necron.mixin;

import cn.boop.necron.gui.ClientButton;
import cn.boop.necron.gui.MainMenu;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.boop.necron.config.impl.NecronOptionsImpl.customMainMenu;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "initGui", at = @At("RETURN"))
    public void onInitGui(CallbackInfo ci) {
        if (customMainMenu) this.mc.displayGuiScreen(new MainMenu());
        this.buttonList.add(new ClientButton(1145, this.width - 55, 5, 50, 18, "Necron"));
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    public void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 1145) {
            customMainMenu = true;
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }
}
