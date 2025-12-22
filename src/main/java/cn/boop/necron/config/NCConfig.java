package cn.boop.necron.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.SubConfig;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cn.boop.necron.Necron;
import cn.boop.necron.config.impl.*;
import cn.boop.necron.module.impl.hud.RngMeterHUD;

public class NCConfig extends Config {
    public NCConfig() {
        super(new Mod(Necron.MODNAME, ModType.THIRD_PARTY), "necronclient.json");
        initialize();
    }

    @SubConfig
    public static AutoClickerOptionsImpl autoClickerOptions = new AutoClickerOptionsImpl();
    @SubConfig
    public static AutoGGOptionsImpl autoGGOptions = new AutoGGOptionsImpl();
    @SubConfig
    public static AutoLeapOptionsImpl autoLeapOptions = new AutoLeapOptionsImpl();
    @SubConfig
    public static AutoPathOptionsImpl autoPathOptions = new AutoPathOptionsImpl();
    @SubConfig
    public static ChatBlockerOptionsImpl chatBlockerOptions = new ChatBlockerOptionsImpl();
    @SubConfig
    public static ChatCommandsOptionsImpl chatCommandsOptions = new ChatCommandsOptionsImpl();
    @SubConfig
    public static GUIOptionsImpl moduleGUIOptions = new GUIOptionsImpl();
    @SubConfig
    public static DungeonOptionsImpl dungeonOptions = new DungeonOptionsImpl();
    @SubConfig
    public static EtherwarpOptionsImpl etherwarpOptions = new EtherwarpOptionsImpl();
    @SubConfig
    public static FakeWipeOptionsImpl fakeWipeOptions = new FakeWipeOptionsImpl();
    @SubConfig
    public static FarmingOptionsImpl farmingOptions = new FarmingOptionsImpl();
    @SubConfig
    public static HurtCameraOptionsImpl hurtCamOptions = new HurtCameraOptionsImpl();
    @SubConfig
    public static NametagsOptionsImpl nametagsOptions = new NametagsOptionsImpl();
    @SubConfig
    public static NecronOptionsImpl necronOptions = new NecronOptionsImpl();
    @SubConfig
    public static RouterOptionsImpl routerOptions = new RouterOptionsImpl();
    @SubConfig
    public static ScrollingOptionsImpl scrollingOptions = new ScrollingOptionsImpl();
    @SubConfig
    public static SlayerOptionsImpl slayerOptions = new SlayerOptionsImpl();
    @SubConfig
    public static TitleOptionsImpl titleOptions = new TitleOptionsImpl();
    @SubConfig
    public static WardrobeOptionsImpl wardrobeOptions = new WardrobeOptionsImpl();
    @SubConfig
    public static WaypointOptionsImpl waypointOptions = new WaypointOptionsImpl();

    @HUD(name = "RNG Meter")
    public static RngMeterHUD rngMeterHUD = new RngMeterHUD();

    public static final NCConfig INSTANCE = new NCConfig();
}
