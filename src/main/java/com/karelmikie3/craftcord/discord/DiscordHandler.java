/*
 * Copyright 2019-2019 karelmikie3
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karelmikie3.craftcord.discord;

import com.google.gson.JsonObject;
import com.karelmikie3.craftcord.config.ModConfig;
import com.karelmikie3.craftcord.util.ColorHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class DiscordHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private DiscordEvents discordEvents;

    private WebhookClient webhookClient;
    private JDA bot;

    private DiscordSetupStatus botStatus = DiscordSetupStatus.STOPPED;
    private DiscordSetupStatus webhookStatus = DiscordSetupStatus.STOPPED;

    private Long webhookID;
    private Long channelID;
    private Long guildID;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.webhookStatus = DiscordSetupStatus.STARTING;
        this.botStatus = DiscordSetupStatus.STARTING;
        this.discordEvents = new DiscordEvents();

        this.discordEvents = new DiscordEvents();

        try {
            this.webhookClient = new WebhookClientBuilder(ModConfig.getWebhookURL()).build();
        } catch (IllegalArgumentException e) {
            this.webhookStatus = DiscordSetupStatus.INVALID_WEBHOOK_URL;
            LOGGER.warn("Invalid webhook URL", e);
        }

        if (this.webhookStatus != DiscordSetupStatus.INVALID_WEBHOOK_URL)
            this.getWebhookData();

        try {
            this.bot = new JDABuilder(AccountType.BOT)
                    .setToken(ModConfig.getBotToken())
                    .build();

            this.bot.addEventListener(discordEvents);
        } catch (LoginException e) {
            LOGGER.warn("Invalid bot authentication token.", e);
            botStatus = DiscordSetupStatus.INVALID_BOT_TOKEN;
        }

    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        if (webhookStatus != DiscordSetupStatus.INVALID_WEBHOOK_URL)
            webhookStatus = DiscordSetupStatus.DONE;

        if (botStatus != DiscordSetupStatus.INVALID_BOT_TOKEN) {
            try {
                bot.awaitReady();
            } catch (InterruptedException e) {
                LOGGER.error("Waiting interrupted, bot may not be available.", e);
            }

            botStatus = DiscordSetupStatus.DONE;
        }

        if (webhookStatus.isUsable() && botStatus.isUsable()) {
            CommonEmoteHelper.setServerEmotes(bot.getGuildById(guildID).getEmotes());
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        webhookStatus = DiscordSetupStatus.STOPPING;
        botStatus = DiscordSetupStatus.STOPPING;

        if (webhookClient != null) {
            webhookClient.close();
            webhookClient = null;
            webhookStatus = DiscordSetupStatus.STOPPED;
        } else {
            webhookStatus = DiscordSetupStatus.STOPPED;
        }

        if (bot != null) {
            bot.removeEventListener(discordEvents);
            bot.shutdownNow();
            bot = null;
            botStatus = DiscordSetupStatus.STOPPED;
        } else {
            botStatus = DiscordSetupStatus.STOPPED;
        }

        CommonEmoteHelper.clearServerEmotes();
        CommonEmoteHelper.clearLocalEmoteCache();
    }

    private void getWebhookData() {
        HttpClient client = HttpClientBuilder.create().build();

        try {
            HttpGet getRequest = new HttpGet(ModConfig.getWebhookURL());
            HttpResponse response = client.execute(getRequest);

            try (InputStream input = response.getEntity().getContent();
                 InputStreamReader reader = new InputStreamReader(input)) {

                JsonObject jsonObject = JSONUtils.fromJson(reader);

                if (jsonObject.has("message")) {
                    String message = jsonObject.get("message").getAsString();
                    int code = jsonObject.get("code").getAsInt();
                    this.webhookStatus = DiscordSetupStatus.INVALID_WEBHOOK_URL;

                    if (Objects.equals(message, "Unknown Webhook")) {
                        LOGGER.warn("Webhook URL is an unknown webhook.");
                    } else if (Objects.equals(message, "Invalid Webhook Token")) {
                        LOGGER.warn("Webhook URL is invalid.");
                    } else {
                        LOGGER.warn("Webhook URL errored with message '{}' and code '{}'.", message, code);
                    }
                } else if (jsonObject.has("name") && jsonObject.has("channel_id") && jsonObject.has("guild_id")) {
                    this.webhookID = jsonObject.get("id").getAsLong();
                    this.channelID = jsonObject.get("channel_id").getAsLong();
                    this.guildID = jsonObject.get("guild_id").getAsLong();
                    LOGGER.debug("Successfully acquired webhook, channel and guild information.");
                } else {
                    LOGGER.warn("Invalid response from Webhook, please check if the Webhook URL is correct.");
                }
            }

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            this.webhookStatus = DiscordSetupStatus.INVALID_WEBHOOK_URL;
            LOGGER.error("Exception on acquiring Webhook data.", e);
        }
    }

    public WebhookClient getWebhookClient() {
        return webhookClient;
    }

    public JDA getBot() {
        return bot;
    }

    public DiscordSetupStatus getBotStatus() {
        return botStatus;
    }

    public DiscordSetupStatus getWebhookStatus() {
        return webhookStatus;
    }

    private class DiscordEvents extends ListenerAdapter {
        private final MinecraftServer SERVER = ServerLifecycleHooks.getCurrentServer();

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if ((!event.isWebhookMessage() || event.getAuthor().getIdLong() != DiscordHandler.this.webhookID) &&
                    (!ModConfig.sameChannel() || event.getChannel().getIdLong() == channelID)) {

                String message = event.getMessage().getContentDisplay();

                for (Emote emote : event.getMessage().getEmotes()) {
                    CommonEmoteHelper.addToLocalEmoteCache(emote);
                    message = message.replace(":" + emote.getName() + ":", ":" + emote.getId() + ":");
                }
                ITextComponent messengerName = new StringTextComponent(event.getMember().getEffectiveName())
                        .applyTextStyle(ColorHelper.getNearestColor(event.getMember().getColor()));

                ITextComponent chatMessage = new TranslationTextComponent("chat.type.discordText", messengerName, message);

                SERVER.getPlayerList().sendMessage(chatMessage);
            }
        }
    }
}
