package com.karelmikie3.craftcord;

import com.karelmikie3.craftcord.chat.ClientChatEvents;
import com.karelmikie3.craftcord.chat.ServerChatEvents;
import com.karelmikie3.craftcord.event.DiscordMessageEvent;
import com.karelmikie3.craftcord.resources.EmoteResourcePack;
import com.karelmikie3.craftcord.util.EmoteHelper;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Mod("craftcord")
public class CraftCord {
    private WebhookClient webhookClient;
    private JDA bot;

    public CraftCord() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initClient);
    }

    private void initClient(final FMLClientSetupEvent event) {
        try {
            EmoteHelper.addEmote("https://cdn.discordapp.com/emojis/461229297122803712.png", "MonkaS");
            EmoteHelper.addEmote("https://cdn.discordapp.com/emojis/375790194936512512.png", "FeelsBadMan");
            EmoteHelper.addEmote("https://cdn.discordapp.com/emojis/472066625454211083.gif", "peek");
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.getMinecraftSupplier().get().getResourceManager().addResourcePack(new EmoteResourcePack());

        MinecraftForge.EVENT_BUS.register(ClientChatEvents.class);
    }

    private ServerChatEvents serverChatEvents;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        webhookClient = new WebhookClientBuilder(Config.webhookURL).build();
        try {
            bot = new JDABuilder(AccountType.BOT)
                    .setToken(Config.botAuthToken)
                    .build()
                    .awaitReady();
        } catch (InterruptedException e) {
            //shouldn't happen but yell at player anyway
            e.printStackTrace();
        } catch (LoginException e) {
            //send player in world message about invalid bot token
            e.printStackTrace();
        }

        bot.addEventListener(new DiscordMessageEvent(event.getServer()));

        serverChatEvents = new ServerChatEvents(webhookClient);
        MinecraftForge.EVENT_BUS.register(serverChatEvents);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        MinecraftForge.EVENT_BUS.unregister(serverChatEvents);
        serverChatEvents = null;
        webhookClient.close();
        webhookClient = null;
        bot.shutdown();
        bot = null;
    }
}
