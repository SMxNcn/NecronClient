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

    /**
     * 加载所有脚本文件
     */
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

    /**
     * 重新加载脚本
     */
    public static void reloadScripts() {
        loadAllScripts();
    }

    /**
     * 加载单个脚本文件
     */
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

    /**
     * 根据文件名加载脚本
     */
    public void loadScriptByName(String fileName) {
        Path filePath = Paths.get(scriptDirectory, fileName);
        if (Files.exists(filePath)) {
            loadScriptFile(filePath);
            rebuildTriggerMap();
        }
    }

    /**
     * 重新构建触发键映射
     */
    private static void rebuildTriggerMap() {
        triggerMap.clear();
        for (Script script : scripts) {
            if (script.isEnabled()) {
                triggerMap.computeIfAbsent(script.getTriggerKey(),
                        k -> new ArrayList<>()).add(script);
            }
        }
    }

    /**
     * 获取指定触发键的脚本列表
     */
    public List<Script> getScriptsByTriggerKey(int keyCode) {
        return triggerMap.getOrDefault(keyCode, new ArrayList<>());
    }

    /**
     * 获取所有脚本
     */
    public List<Script> getAllScripts() {
        return new ArrayList<>(scripts);
    }

    /**
     * 根据名称获取脚本
     */
    public Script getScriptByName(String name) {
        return scripts.stream()
                .filter(script -> script.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 启用/禁用脚本
     */
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
