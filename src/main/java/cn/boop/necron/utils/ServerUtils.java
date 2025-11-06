package cn.boop.necron.utils;

import cn.boop.necron.Necron;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;

public class ServerUtils {
    public static ServerData lastServerData = null;
    public static GuiScreen parentScreen;

    public static void reconnectToServer() {
        ServerAddress serverAddress = ServerAddress.fromString(lastServerData.serverIP);
        Necron.mc.displayGuiScreen(new GuiConnecting(
                parentScreen,
                Necron.mc,
                serverAddress.getIP(),
                serverAddress.getPort()
        ));
    }
}
