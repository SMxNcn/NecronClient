package cn.boop.necron.config;

import cn.boop.necron.Necron;
import cn.boop.necron.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateChecker {

    private static final String USER_AGENT = "Necron-Checker";
    private final String currentVersion;

    private static final AtomicBoolean hasChecked = new AtomicBoolean(false);
    private static final AtomicReference<String> latestVersionRef = new AtomicReference<>("");

    public UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public void checkForUpdates() {
        if (hasChecked.getAndSet(true)) return;

        new Thread(() -> {
            try {
                URL url = new URL("https://gitee.com/mixturedg/necron-client-repo/raw/master/version.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                    String latestVersion = json.get("version").getAsString().replaceAll("^v", "");

                    ComparableVersion current = new ComparableVersion(currentVersion.replaceAll("^v", ""));
                    ComparableVersion latest = new ComparableVersion(latestVersion);

                    if (latest.compareTo(current) > 0) {
                        latestVersionRef.set(latestVersion);
                        onNewVersionAvailable(currentVersion, latestVersion);
                    }

                } else {
                    Utils.modMessage("§cHTTP Error: " + responseCode);
                }
            } catch (IOException e) {
                Utils.modMessage("§c" + e.getMessage());
            } catch (Exception ignored) {
            }
        }, "Update Checker").start();
    }

    public void onNewVersionAvailable(String current, String latest) {
        Utils.modMessage("New version available! §8" + current + "§7 ->§a " + latest);
        Necron.mc.thePlayer.addChatMessage(new ChatComponentText("§bNecron §8»§r §a§nClick to download")
                .setChatStyle(new ChatStyle()
                        .setUnderlined(true)
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gitee.com/mixturedg/necron-client/releases/latest"))
                ));
    }
}