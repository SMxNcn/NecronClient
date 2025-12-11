package cn.boop.necron.events;

import cn.boop.necron.config.impl.NecronOptionsImpl;
import cn.boop.necron.config.script.Script;
import cn.boop.necron.config.script.ScriptExecutor;
import cn.boop.necron.config.script.ScriptManager;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ScriptKeyEventHandler {
    private static ScriptManager scriptManager;
    private static final boolean[] keyPressed = new boolean[256]; // 记录按键状态

    public static void setScriptManager(ScriptManager manager) {
        scriptManager = manager;
        MinecraftForge.EVENT_BUS.register(new ScriptKeyEventHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && scriptManager != null) {
            handleKeyPress();
        }
    }

    private void handleKeyPress() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        for (int keyCode = 0; keyCode < 256; keyCode++) {
            boolean isPressed = Keyboard.isKeyDown(keyCode);

            if (isPressed && !keyPressed[keyCode]) {
                executeScriptsForKey(keyCode);
            }
            keyPressed[keyCode] = isPressed;
        }
    }


    private void executeScriptsForKey(int key) {
        if (scriptManager == null) return;

        List<Script> triggeredScripts = scriptManager.getScriptsByTriggerKey(key);

        for (Script script : triggeredScripts) {
            if (script.isEnabled()) {
                if (NecronOptionsImpl.executeMessage) Utils.modMessage("Executing script §a[" + script.getName() + "]§7on key §8[" + script.getTriggerKey() + "]");
                ScriptExecutor.executeScript(script);
            }
        }
    }
}