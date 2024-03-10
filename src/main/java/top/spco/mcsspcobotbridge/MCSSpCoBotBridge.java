package top.spco.mcsspcobotbridge;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import top.spco.mcsspcobotbridge.bridge.BridgeServer;
import top.spco.mcsspcobotbridge.bridge.Payload;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MCSSpCoBotBridge.MODID)
public class MCSSpCoBotBridge {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mssbb";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private BridgeServer server;

    public MCSSpCoBotBridge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MSSBB Starting...");
        server = BridgeServer.getInstance();
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "PLAYER_LOGGED_IN");
        data.addProperty("player_name", event.getEntity().getName().getString());
        this.server.getClientManager().pushToAll(Payload.pushEvent(data));
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "PLAYER_LOGGED_OUT");
        data.addProperty("player_name", event.getEntity().getName().getString());
        this.server.getClientManager().pushToAll(Payload.pushEvent(data));
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "CHAT");
        data.addProperty("sender_name", event.getPlayer().getName().getString());
        data.addProperty("message", event.getMessage().getString());
        this.server.getClientManager().pushToAll(Payload.pushEvent(data));
    }
}
