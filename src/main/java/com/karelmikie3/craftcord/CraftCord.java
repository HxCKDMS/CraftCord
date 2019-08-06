package com.karelmikie3.craftcord;

import com.karelmikie3.craftcord.chat.ClientChatEvents;
import com.karelmikie3.craftcord.chat.ServerChatEvents;
import com.karelmikie3.craftcord.config.CommonModConfig;
import com.karelmikie3.craftcord.discord.DiscordHandler;
import com.karelmikie3.craftcord.event.EntityEvents;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import com.karelmikie3.craftcord.proxy.ClientProxy;
import com.karelmikie3.craftcord.proxy.IProxy;
import com.karelmikie3.craftcord.proxy.ServerProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.karelmikie3.craftcord.CraftCord.MOD_ID;

//TODO: add preset configs, client and local configs.
//TODO: keep emotes on disk instead of in memory and add option to just use memory.
//TODO: replace '[DISCORD]' in chat with the Discord logo.
@Mod(MOD_ID)
public class CraftCord {
    public static final String MOD_ID = "craftcord";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static CraftCord INSTANCE;
    private static DiscordHandler DISCORD_HANDLER;
    private static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static CraftCord getInstance() {
        return INSTANCE;
    }

    public CraftCord() {
        INSTANCE = this;
        DISCORD_HANDLER = new DiscordHandler();
        proxy.constructor();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        MinecraftForge.EVENT_BUS.register(DISCORD_HANDLER);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        CommonModConfig.initConfig();

    }

    private void setup(final FMLCommonSetupEvent event) {
        proxy.setup();
        System.out.println("setup!");
        int id = 0;
        NETWORK.messageBuilder(SendEmotesMessageS2C.class, id++)
                .encoder(SendEmotesMessageS2C::encode)
                .decoder(SendEmotesMessageS2C::decode)
                .consumer(SendEmotesMessageS2C::handle)
                .add();

        NETWORK.messageBuilder(RequestEmoteMessageC2S.class, id++)
                .encoder(RequestEmoteMessageC2S::encode)
                .decoder(RequestEmoteMessageC2S::decode)
                .consumer(RequestEmoteMessageC2S::handle)
                .add();

    }

    private void initClient(final FMLClientSetupEvent event) {
        System.out.println("init client");

        MinecraftForge.EVENT_BUS.register(new ClientChatEvents());
    }

    private ServerChatEvents serverChatEvents;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        serverChatEvents = new ServerChatEvents(DISCORD_HANDLER);
        MinecraftForge.EVENT_BUS.register(serverChatEvents);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        MinecraftForge.EVENT_BUS.unregister(serverChatEvents);
        serverChatEvents = null;
    }
}
