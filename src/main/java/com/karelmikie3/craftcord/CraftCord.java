package com.karelmikie3.craftcord;

import com.karelmikie3.craftcord.chat.ClientChatEvents;
import com.karelmikie3.craftcord.chat.ServerChatEvents;
import com.karelmikie3.craftcord.event.DiscordMessageEvent;
import com.karelmikie3.craftcord.event.EntityEvents;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import com.karelmikie3.craftcord.resources.EmoteResourcePack;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.security.auth.login.LoginException;
import java.util.UUID;

import static com.karelmikie3.craftcord.CraftCord.MOD_ID;

@Mod(MOD_ID)
public class CraftCord {
    public static final String MOD_ID = "craftcord";
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private WebhookClient webhookClient;
    private JDA bot;
    private static CraftCord INSTANCE;

    public WebhookClient getWebhookClient() {
        return webhookClient;
    }

    public JDA getBot() {
        return bot;
    }

    public static CraftCord getInstance() {
        return INSTANCE;
    }

    public CraftCord() {
        INSTANCE = this;

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
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
        /*try {
            ClientEmoteHelper.addEmote("https://cdn.discordapp.com/emojis/461229297122803712.png", "MonkaS", true);
            ClientEmoteHelper.addEmote("https://cdn.discordapp.com/emojis/375790194936512512.png", "FeelsBadMan", true);
            ClientEmoteHelper.addEmote("https://cdn.discordapp.com/emojis/472066625454211083.gif", "peek", true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        event.getMinecraftSupplier().get().getResourceManager().addResourcePack(new EmoteResourcePack());

        MinecraftForge.EVENT_BUS.register(new ClientChatEvents());
    }

    private ServerChatEvents serverChatEvents;
    private UUID sessionUUID;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        sessionUUID = UUID.randomUUID();

        webhookClient = new WebhookClientBuilder(Config.webhookURL).build();
        try {
            bot = new JDABuilder(AccountType.BOT)
                    .setToken(Config.botAuthToken)
                    .build();
        } catch (LoginException e) {
            //send player in world message about invalid bot token
            e.printStackTrace();
        }

        bot.addEventListener(new DiscordMessageEvent(sessionUUID));

        serverChatEvents = new ServerChatEvents(webhookClient, bot);
        MinecraftForge.EVENT_BUS.register(serverChatEvents);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        try {
            bot.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        webhookClient.send("Server + Channel check: " + sessionUUID.toString());
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        CommonEmoteHelper.removeServerEmotes();
        MinecraftForge.EVENT_BUS.unregister(serverChatEvents);
        serverChatEvents = null;
        webhookClient.close();
        webhookClient = null;
        bot.shutdownNow();
        bot = null;
    }
}
