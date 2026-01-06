package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import cn.boop.necron.module.impl.ChatCommands;
import cn.boop.necron.module.impl.Waypoint;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Waypoint.class, new WaypointSerializer())
            .registerTypeAdapter(Waypoint.class, new WaypointDeserializer())
            .setPrettyPrinting()
            .create();

    public static List<String> loadTips() {
        try (InputStream is = ChatCommands.class.getResourceAsStream("/tips.json")) {
            if (is == null) throw new IOException("tips.json not found");
            JsonObject json = GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
            return GSON.fromJson(json.get("tips"), new TypeToken<List<String>>(){}.getType());
        } catch (Exception e) {
            Necron.LOGGER.error("Error loading tips.json");
            return getDefaultTips();
        }
    }

    private static List<String> getDefaultTips() {
        return Arrays.asList(
            "(Default) JSON file error",
            "(Default) Try to join SkyBlock",
            "(Default) Wither Impact (-150 Mana)"
        );
    }

    public static List<Waypoint> loadWaypoints(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            JsonElement jsonElement = new JsonParser().parse(content);

            List<Waypoint> waypoints = new ArrayList<>();
            String islandName = null;
            String typeName = "Router";

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("type")) {
                    typeName = jsonObject.get("type").getAsString();
                }

                if (jsonObject.has("island")) {
                    islandName = jsonObject.get("island").getAsString();
                }

                if (jsonObject.has("waypoints")) {
                    waypoints = GSON.fromJson(jsonObject.get("waypoints"), new TypeToken<List<Waypoint>>(){}.getType());
                }
            } else if (jsonElement.isJsonArray()) {
                waypoints = GSON.fromJson(jsonElement, new TypeToken<List<Waypoint>>(){}.getType());
                Necron.LOGGER.info("Loaded waypoints from old format (no metadata)");
            }

            try {
                Waypoint.TYPE requiredType = Waypoint.TYPE.valueOf(typeName);
                Waypoint.setCurrentType(requiredType);
            } catch (IllegalArgumentException e) {
                Necron.LOGGER.warn("Unknown type name in waypoint file: {}, using default: [Router]", typeName);
                Waypoint.setCurrentType(Waypoint.TYPE.Router);
            }

            if (islandName != null) {
                try {
                    LocationUtils.Island requiredIsland = LocationUtils.Island.valueOf(islandName);
                    Waypoint.setRequiredIsland(requiredIsland);
                } catch (IllegalArgumentException e) {
                    Necron.LOGGER.warn("Unknown island name in waypoint file: {}", islandName);
                }
            } else {
                Waypoint.setRequiredIsland(null);
            }

            return waypoints;
        } catch (Exception e) {
            Necron.LOGGER.error("Error loading waypoints: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean saveWaypoints(List<Waypoint> waypoints, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("type", Waypoint.getCurrentType().name());

            if (Waypoint.getRequiredIsland() != null) {
                jsonObject.addProperty("island", Waypoint.getRequiredIsland().name());
            }

            jsonObject.add("waypoints", GSON.toJsonTree(waypoints));

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
                GSON.toJson(jsonObject, writer);
            }
            return true;
        } catch (Exception e) {
            Necron.LOGGER.error("Error saving waypoints: {}", e.getMessage());
            return false;
        }
    }

    public static List<String> loadWhitelist() {
        try {
            Path whitelistPath = Paths.get("config/necron/whitelist.json");
            if (!Files.exists(whitelistPath)) {
                return new ArrayList<>();
            }
            String content = new String(Files.readAllBytes(whitelistPath), StandardCharsets.UTF_8);
            return GSON.fromJson(content, new TypeToken<List<String>>(){}.getType());
        } catch (Exception e) {
            Necron.LOGGER.error("Error loading whitelist: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveWhitelist(List<String> whitelist) {
        try {
            Path whitelistPath = Paths.get("config/necron/whitelist.json");
            Files.createDirectories(whitelistPath.getParent());
            if (!Files.exists(whitelistPath)) {
                Files.createFile(whitelistPath);
            }
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(whitelistPath), StandardCharsets.UTF_8)) {
                GSON.toJson(whitelist, writer);
            }
        } catch (Exception e) {
            Necron.LOGGER.error("Error saving whitelist: {}", e.getMessage());
        }
    }

    public static Set<String> loadProtectedItems() {
        try {
            Path protectedItemsPath = Paths.get("config/necron/protected_items.json");
            if (!Files.exists(protectedItemsPath)) {
                return new HashSet<>();
            }
            String content = new String(Files.readAllBytes(protectedItemsPath), StandardCharsets.UTF_8);
            return GSON.fromJson(content, new TypeToken<Set<String>>(){}.getType());
        } catch (Exception e) {
            Necron.LOGGER.error("Error loading protected items: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    public static void saveProtectedItems(Set<String> protectedItems) {
        try {
            Path protectedItemsPath = Paths.get("config/necron/protected_items.json");
            Files.createDirectories(protectedItemsPath.getParent());
            if (!Files.exists(protectedItemsPath)) {
                Files.createFile(protectedItemsPath);
            }
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(protectedItemsPath), StandardCharsets.UTF_8)) {
                GSON.toJson(protectedItems, writer);
            }
        } catch (Exception e) {
            Necron.LOGGER.error("Error saving protected items: {}", e.getMessage());
        }
    }

    private static class WaypointSerializer implements JsonSerializer<Waypoint> {
        @Override
        public JsonElement serialize(Waypoint waypoint, java.lang.reflect.Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", waypoint.getId());
            obj.addProperty("x", waypoint.getX());
            obj.addProperty("y", waypoint.getY());
            obj.addProperty("z", waypoint.getZ());
            obj.addProperty("direction", waypoint.getDirection());
            obj.addProperty("rotation", waypoint.getRotation());

            if (waypoint.getName() != null) {
                obj.addProperty("name", waypoint.getName());
            }

            return obj;
        }
    }

    private static class WaypointDeserializer implements JsonDeserializer<Waypoint> {
        @Override
        public Waypoint deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int id = obj.get("id").getAsInt();
            int x = obj.get("x").getAsInt();
            int y = obj.get("y").getAsInt();
            int z = obj.get("z").getAsInt();

            String direction = "forward";
            float rotation = 0.0f;
            String name = null;

            if (obj.has("direction")) {
                direction = obj.get("direction").getAsString();
            }

            if (obj.has("rotation")) {
                rotation = obj.get("rotation").getAsFloat();
            }

            if (obj.has("name")) {
                name = obj.get("name").getAsString();
            }

            Waypoint waypoint = new Waypoint(id, x, y, z, direction, rotation);
            if (name != null) {
                waypoint.setName(name);
            }


            return waypoint;
        }
    }
}