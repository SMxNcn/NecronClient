package cn.boop.necron.config.script;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptManager {
    private static List<Script> scripts;
    private static Map<Integer, List<Script>> triggerMap;
    private static final String scriptDirectory = Necron.mc.mcDataDir + "/config/necron/scripts/";

    public ScriptManager() {
        scripts = new ArrayList<>();
        triggerMap = new HashMap<>();
        try {
            Files.createDirectories(Paths.get(scriptDirectory));
        } catch (IOException e) {
            Necron.LOGGER.error("Failed to create script directory: {}", scriptDirectory);
        }
        loadAllScripts();
    }

    public static void loadAllScripts() {
        clearScripts();
        try {
            Files.walk(Paths.get(scriptDirectory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(ScriptManager::loadScriptFile);
        } catch (IOException e) {
            Necron.LOGGER.error("Failed to load script file", e);
        }
        rebuildTriggerMap();
    }

    public static void reloadScripts() {
        loadAllScripts();
    }

    private static void loadScriptFile(Path filePath) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            List<Script> parsedScripts = ScriptParser.parse(content);
            scripts.addAll(parsedScripts);
            if (Necron.mc.thePlayer != null) Utils.modMessage("Loaded " + scripts.size() + " scripts.");
            else Necron.LOGGER.info("Loaded {} scripts.", scripts.size());
        } catch (IOException e) {
            Necron.LOGGER.error("Failed to load script file: {}", filePath, e);
        }
    }

    public void loadScriptByName(String fileName) {
        Path filePath = Paths.get(scriptDirectory, fileName);
        if (Files.exists(filePath)) {
            loadScriptFile(filePath);
            rebuildTriggerMap();
        }
    }

    private static void rebuildTriggerMap() {
        triggerMap.clear();
        for (Script script : scripts) {
            if (script.isEnabled()) {
                triggerMap.computeIfAbsent(script.getTriggerKey(),
                        k -> new ArrayList<>()).add(script);
            }
        }
    }

    public List<Script> getScriptsByTriggerKey(int keyCode) {
        return triggerMap.getOrDefault(keyCode, new ArrayList<>());
    }

    public List<Script> getAllScripts() {
        return new ArrayList<>(scripts);
    }

    public Script getScriptByName(String name) {
        return scripts.stream()
                .filter(script -> script.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void setScriptEnabled(String name, boolean enabled) {
        Script script = getScriptByName(name);
        if (script != null) {
            script.setEnabled(enabled);
            rebuildTriggerMap();
        }
    }

    private static void clearScripts() {
        scripts.clear();
        triggerMap.clear();
    }
}
