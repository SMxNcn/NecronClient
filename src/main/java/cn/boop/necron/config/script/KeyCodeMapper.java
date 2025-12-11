package cn.boop.necron.config.script;

import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class KeyCodeMapper {
    private static final Map<String, Integer> keyNameMap = new HashMap<>();

    static {
        keyNameMap.put("KEY_0", Keyboard.KEY_0);
        keyNameMap.put("KEY_1", Keyboard.KEY_1);
        keyNameMap.put("KEY_2", Keyboard.KEY_2);
        keyNameMap.put("KEY_3", Keyboard.KEY_3);
        keyNameMap.put("KEY_4", Keyboard.KEY_4);
        keyNameMap.put("KEY_5", Keyboard.KEY_5);
        keyNameMap.put("KEY_6", Keyboard.KEY_6);
        keyNameMap.put("KEY_7", Keyboard.KEY_7);
        keyNameMap.put("KEY_8", Keyboard.KEY_8);
        keyNameMap.put("KEY_9", Keyboard.KEY_9);

        keyNameMap.put("KEY_NUMPAD0", Keyboard.KEY_NUMPAD0);
        keyNameMap.put("KEY_NUMPAD1", Keyboard.KEY_NUMPAD1);
        keyNameMap.put("KEY_NUMPAD2", Keyboard.KEY_NUMPAD2);
        keyNameMap.put("KEY_NUMPAD3", Keyboard.KEY_NUMPAD3);
        keyNameMap.put("KEY_NUMPAD4", Keyboard.KEY_NUMPAD4);
        keyNameMap.put("KEY_NUMPAD5", Keyboard.KEY_NUMPAD5);
        keyNameMap.put("KEY_NUMPAD6", Keyboard.KEY_NUMPAD6);
        keyNameMap.put("KEY_NUMPAD7", Keyboard.KEY_NUMPAD7);
        keyNameMap.put("KEY_NUMPAD8", Keyboard.KEY_NUMPAD8);
        keyNameMap.put("KEY_NUMPAD9", Keyboard.KEY_NUMPAD9);

        keyNameMap.put("KEY_ESCAPE", Keyboard.KEY_ESCAPE);
        keyNameMap.put("KEY_ENTER", Keyboard.KEY_RETURN);
        keyNameMap.put("KEY_SPACE", Keyboard.KEY_SPACE);
        keyNameMap.put("KEY_LSHIFT", Keyboard.KEY_LSHIFT);
        keyNameMap.put("KEY_RSHIFT", Keyboard.KEY_RSHIFT);
        keyNameMap.put("KEY_LCTRL", Keyboard.KEY_LCONTROL);
        keyNameMap.put("KEY_RCTRL", Keyboard.KEY_RCONTROL);

        keyNameMap.put("KEY_UP", Keyboard.KEY_UP);
        keyNameMap.put("KEY_DOWN", Keyboard.KEY_DOWN);
        keyNameMap.put("KEY_LEFT", Keyboard.KEY_LEFT);
        keyNameMap.put("KEY_RIGHT", Keyboard.KEY_RIGHT);

        keyNameMap.put("KEY_A", Keyboard.KEY_A);
        keyNameMap.put("KEY_B", Keyboard.KEY_B);
        keyNameMap.put("KEY_C", Keyboard.KEY_C);
        keyNameMap.put("KEY_D", Keyboard.KEY_D);
        keyNameMap.put("KEY_E", Keyboard.KEY_E);
        keyNameMap.put("KEY_F", Keyboard.KEY_F);
        keyNameMap.put("KEY_G", Keyboard.KEY_G);
        keyNameMap.put("KEY_H", Keyboard.KEY_H);
        keyNameMap.put("KEY_I", Keyboard.KEY_I);
        keyNameMap.put("KEY_J", Keyboard.KEY_J);
        keyNameMap.put("KEY_K", Keyboard.KEY_K);
        keyNameMap.put("KEY_L", Keyboard.KEY_L);
        keyNameMap.put("KEY_M", Keyboard.KEY_M);
        keyNameMap.put("KEY_N", Keyboard.KEY_N);
        keyNameMap.put("KEY_O", Keyboard.KEY_O);
        keyNameMap.put("KEY_P", Keyboard.KEY_P);
        keyNameMap.put("KEY_Q", Keyboard.KEY_Q);
        keyNameMap.put("KEY_R", Keyboard.KEY_R);
        keyNameMap.put("KEY_S", Keyboard.KEY_S);
        keyNameMap.put("KEY_T", Keyboard.KEY_T);
        keyNameMap.put("KEY_U", Keyboard.KEY_U);
        keyNameMap.put("KEY_V", Keyboard.KEY_V);
        keyNameMap.put("KEY_W", Keyboard.KEY_W);
        keyNameMap.put("KEY_X", Keyboard.KEY_X);
        keyNameMap.put("KEY_Y", Keyboard.KEY_Y);
        keyNameMap.put("KEY_Z", Keyboard.KEY_Z);
    }

    public static int getKeyCode(String keyName) {
        return keyNameMap.getOrDefault(keyName.toUpperCase(), -1);
    }

    public static boolean isValidKeyName(String keyName) {
        return keyNameMap.containsKey(keyName.toUpperCase());
    }
}
