package cn.boop.necron.module;

import cn.boop.necron.config.impl.*;
import cn.boop.necron.events.*;
import cn.boop.necron.gui.MainMenu;
import cn.boop.necron.module.impl.*;
import cn.boop.necron.module.impl.hud.ModuleList;
import cn.boop.necron.utils.DungeonUtils;
import cn.boop.necron.utils.LocationUtils;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final ArrayList<Object> modules = new ArrayList<>();
    
    public static void initModules() {
        // Modules
        modules.add(new AutoClicker());
        modules.add(new AutoGG());
        modules.add(new AutoI4());
        modules.add(new AutoPath());
        modules.add(new AutoWardrobe());
        modules.add(new ChatBlocker());
        modules.add(new ChatCommands());
        modules.add(new CropNuker());
        modules.add(new DungeonESP());
        modules.add(new Etherwarp());
        modules.add(new EtherwarpRouter());
        modules.add(new FailSafe());
        modules.add(new FakeWipe());
        modules.add(new HurtCam());
        modules.add(new ModuleList());
        modules.add(new M7Waypoints());
        modules.add(new Nametags());
        modules.add(new TitleManager());

        // Other utils/events
        modules.add(new DungeonUtils());
        modules.add(new LocationUtils());
        modules.add(new MainMenu());
        modules.add(new B64ChatEventHandler());
        modules.add(new DungeonRngEventHandler());
        modules.add(new LootEventHandler());
        modules.add(new SlayerEventHandler());
        modules.add(new SlayerRngEventHandler());
        modules.add(new WaypointEventHandler());

        for (Object module : modules) {
            MinecraftForge.EVENT_BUS.register(module);
        }
    }

    public static List<String> getActiveModules() {
        /*
         *  有点抽象的写法 0.o
         */
        List<String> activeModules = new ArrayList<>();
        if (AutoClickerOptionsImpl.autoClicker) activeModules.add("AutoClicker");
        if (AutoGGOptionsImpl.autoGG) activeModules.add("AutoGG");
        if (AutoPathOptionsImpl.autoPath) activeModules.add("AutoPath");
        if (WardrobeOptionsImpl.wardrobe) activeModules.add("AutoWardrobe");
        if (ChatCommandsOptionsImpl.chatCommands) activeModules.add("ChatCommands");
        if (FarmingOptionsImpl.cropNuker) activeModules.add("CropNuker");
        if (EtherwarpOptionsImpl.etherwarp) activeModules.add("Etherwarp");
        if (RouterOptionsImpl.router) activeModules.add("EtherwarpRouter");
        if (HurtCameraOptionsImpl.hurtCam) activeModules.add("HurtCam");
        if (NametagsOptionsImpl.nametags) activeModules.add("Nametags");
        if (DungeonOptionsImpl.autoI4) activeModules.add("AutoI4");
        if (WaypointOptionsImpl.waypoints) activeModules.add("Waypoints");

        return activeModules;
    }
}
