package cn.boop.necron.config.script;

import cn.boop.necron.config.script.actions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptParser {
    enum ParseState {
        SCRIPT_LEVEL,    // 脚本级别指令解析
        ACTION_BLOCK     // 动作块内解析
    }

    private static Script parseScriptHeader(String line) {
        Pattern headerPattern = Pattern.compile("\\*Start\\(\\[(.*?)]\\[(.*?)]\\)");
        Matcher matcher = headerPattern.matcher(line);

        String name = "Unnamed";
        boolean enabled = true;

        if (matcher.find()) {
            name = matcher.group(1).trim();
            enabled = Boolean.parseBoolean(matcher.group(2).trim());
        }

        Script script = new Script(name, -1, 0, enabled);
        script.setEnabled(enabled);
        return script;
    }

    public static List<Script> parse(String content) {
        List<Script> scripts = new ArrayList<>();
        String[] lines = content.split("\n");

        Script currentScript = null;
        List<Action> currentActions = null;
        ParseState state = ParseState.SCRIPT_LEVEL;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("*Start")) {
                currentScript = parseScriptHeader(line);
                currentActions = new ArrayList<>();
                state = ParseState.SCRIPT_LEVEL;
            } else if (line.startsWith("*End")) {
                finalizeScript(currentScript, currentActions, scripts);
                currentScript = null;
                currentActions = null;
                state = ParseState.SCRIPT_LEVEL;
            } else {
                state = processLine(line, currentScript, currentActions, state);
            }
        }

        return scripts;
    }

    private static void finalizeScript(Script script, List<Action> actions, List<Script> scripts) {
        if (script != null && actions != null) {
            for (Action action : actions) {
                script.addAction(action);
            }
            scripts.add(script);
        }
    }

    private static ParseState processLine(String line, Script script, List<Action> actions, ParseState currentState) {
        if (line.startsWith("-Action:")) {
            return ParseState.ACTION_BLOCK;
        } else if (currentState == ParseState.ACTION_BLOCK) {
            if (line.equals("]")) {
                return ParseState.SCRIPT_LEVEL;
            } else if (line.startsWith("-") && actions != null) {
                Action action = parseAction(line);
                if (action != null) {
                    actions.add(action);
                }
            }
        } else if (currentState == ParseState.SCRIPT_LEVEL && line.startsWith("-") && script != null) {
            if (line.startsWith("-TriggerKey:") || line.startsWith("-Delay:")) {
                parseScriptLevelDirective(script, line);
            } else {
                Action action = parseAction(line);
                if (action != null && actions != null) {
                    actions.add(action);
                }
            }
        }
        return currentState;
    }

    private static long parseDelay(String delayStr) {
        if (delayStr.endsWith("t")) {
            int ticks = Integer.parseInt(delayStr.substring(0, delayStr.length() - 1));
            return ticks * 50L;
        } else {
            return Long.parseLong(delayStr);
        }
    }

    private static Action parseAction(String line) {
        String actionType = line.substring(1, line.indexOf(":")).trim();
        switch (actionType) {
            case "ClickSlot":
                return new ClickSlotAction(Integer.parseInt(line.substring(11).trim()), 0);
            case "SendChat":
                return new SendChatAction(line.substring(10).trim(), 0, false);
            case "SendCmd":
                return new SendChatAction(line.substring(9).trim(), 0, true);
            case "Delay":
                return new DelayAction(parseDelay(line.substring(7).trim()));
            case "UseKey":
                return new UseKeyAction(KeyCodeMapper.getKeyCode(line.substring(8).trim()), 0);
            case "SendClient":
                return new SendChatComponentAction(line.substring(11).trim(), 0, false);
            default:
                return null;
        }
    }

    private static void parseScriptLevelDirective(Script script, String line) {
        if (line.startsWith("-TriggerKey:")) {
            String keyStr = line.substring(12).trim();
            int key;

            if (keyStr.startsWith("KEY_")) {
                key = KeyCodeMapper.getKeyCode(keyStr);
                if (key == -1) {
                    System.err.println("Unknown key name: " + keyStr);
                    key = -1;
                }
            } else {
                try {
                    key = Integer.parseInt(keyStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid key code: " + keyStr);
                    key = -1;
                }
            }
            script.setTriggerKey(key);
        } else if (line.startsWith("-ScDelay:")) {
            String delayStr = line.substring(7).trim();
            long delay = parseDelay(delayStr);
            script.setInitialDelay(delay);
        }
    }
}