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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            return GSON.fromJson(content, new TypeToken<List<Waypoint>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean saveWaypoints(List<Waypoint> waypoints, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
                GSON.toJson(waypoints, writer);
            }
            return true;
        } catch (Exception e) {
            Necron.LOGGER.error("Error saving waypoints: {}", e.getMessage());
            return false;
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

            if (obj.has("direction")) {
                direction = obj.get("direction").getAsString();
            }

            if (obj.has("rotation")) {
                rotation = obj.get("rotation").getAsFloat();
            }

            return new Waypoint(id, x, y, z, direction, rotation);
        }
    }
}
