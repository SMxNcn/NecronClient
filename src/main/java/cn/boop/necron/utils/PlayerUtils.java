package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class PlayerUtils {
    public static void setSneak(boolean sneak) {
         KeyBinding.setKeyBindState(Necron.mc.gameSettings.keyBindSneak.getKeyCode(), sneak);
    }

    public static void rightClick() {
        KeyBinding.onTick(Necron.mc.gameSettings.keyBindUseItem.getKeyCode());
    }

    public static void leftClick() {
        KeyBinding.onTick(Necron.mc.gameSettings.keyBindAttack.getKeyCode());
    }

    public static String getKeyName(int keyCode) {
        return keyCode < 0 ? Mouse.getButtonName(keyCode + 100) : Keyboard.getKeyName(keyCode);
    }

    public static void updateKeyState(int keyCode) {
        setKeyBindState(keyCode, keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode));
    }

    public static void setKeyBindState(int keyCode, boolean pressed) {
        KeyBinding.setKeyBindState(keyCode, pressed);
    }

    public static void pressKeyOnce(int keyCode) {
        KeyBinding.onTick(keyCode);
    }
}
