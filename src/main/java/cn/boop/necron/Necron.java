package cn.boop.necron;

import cn.boop.necron.command.ClientCommands;
import cn.boop.necron.command.DebugCommands;
import cn.boop.necron.config.FontManager;
import cn.boop.necron.config.NCConfig;
import cn.boop.necron.config.UpdateChecker;
import cn.boop.necron.config.script.ScriptManager;
import cn.boop.necron.events.ScriptKeyEventHandler;
import cn.boop.necron.module.ModuleManager;
import cn.boop.necron.module.impl.item.ItemProtector;
import cn.boop.necron.module.impl.rng.DungeonRngManager;
import cn.boop.necron.module.impl.rng.RngManager;
import cn.boop.necron.module.impl.rng.SlayerRngManager;
import cn.boop.necron.module.impl.slayer.AatroxBuffChecker;
import cn.boop.necron.utils.LocationUtils;
import cn.boop.necron.utils.RotationUtils;
import cn.boop.necron.utils.SystemUtils;
import cn.boop.necron.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

@Mod(modid = Necron.MODID, name = Necron.MODNAME, version = Necron.VERSION, acceptedMinecraftVersions = "1.8.9", clientSideOnly = true)
public class Necron {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static final String MODID = "necronclient";
    public static final String MODNAME = "Necron";
    public static final String VERSION = "0.1.8";
    public static final String WP_FILE_DIR = "./config/necron/waypoints/";
    public static final String BG_FILE_DIR = "./config/necron/backgrounds/";
    public static final Logger LOGGER = LogManager.getLogger(Necron.class);

    private static boolean playerEnteredWorld = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SystemUtils.INSTANCE.initializeTray();
        ModuleManager.initModules();
        ItemProtector.initUuids();
        FontManager.initFonts();

        Map<String, Map<String, Integer>> rngMeterValues = RngManager.loadRngMeterValues();
        DungeonRngManager.INSTANCE.loadValuesFile(rngMeterValues);
        SlayerRngManager.INSTANCE.loadValuesFile(rngMeterValues);
        File dataFile = new File(mc.mcDataDir, "config/necron/data/data.json");
        DungeonRngManager.INSTANCE.setDataFile(dataFile);
        SlayerRngManager.INSTANCE.setDataFile(dataFile);
        ScriptKeyEventHandler.setScriptManager(new ScriptManager());

        DungeonRngManager.INSTANCE.load();
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ClientCommands());
        ClientCommandHandler.instance.registerCommand(new DebugCommands());
        NCConfig.INSTANCE.preload();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            RotationUtils.updateRotations();
        }

        if (event.phase == TickEvent.Phase.END) {
            if (mc.thePlayer != null && !playerEnteredWorld) {
                playerEnteredWorld = true;
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        new UpdateChecker(VERSION).checkForUpdates();
                        AatroxBuffChecker.initialize();
                    } catch (InterruptedException ignored) {}
                }).start();
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) throws Exception {
        if (event.type != 0 || !LocationUtils.inSkyBlock) return;
        String cleanMessage = Utils.removeFormatting(event.message.getFormattedText());
        if ("Everybody unlocks exclusive perks!".equals(cleanMessage)) AatroxBuffChecker.fetchElectionData();
    }

    @Mod.EventHandler
    public void onShutdown(FMLServerStoppedEvent event) {
        SystemUtils.INSTANCE.cleanup();
    }
}
