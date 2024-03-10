package top.spco.mcsspcobotbridge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = MCSSpCoBotBridge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> NAME = BUILDER
            .comment("Server name.")
            .define("name", "server");
    private static final ForgeConfigSpec.IntValue PORT = BUILDER
            .comment("Port of the MSSBB server.")
            .defineInRange("port", 58964, 1024, 65535);
    private static final ForgeConfigSpec.IntValue HEARTBEAT_INTERVAL = BUILDER
            .comment("Heartbeat interval (milliseconds).")
            .defineInRange("heartbeat_interval", 5000, 5000, 10000);

    private static final ForgeConfigSpec.BooleanValue DEBUG = BUILDER.define("debug", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String name;
    public static int port;
    public static int heartbeatInterval;
    public static boolean debug;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        port = PORT.get();
        heartbeatInterval = HEARTBEAT_INTERVAL.get();
        name = NAME.get();
        debug = DEBUG.get();
    }
}
