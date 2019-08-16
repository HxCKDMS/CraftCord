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
import com.karelmikie3.craftcord.api.presence.IMinecraftPresence;
import com.karelmikie3.craftcord.api.status.IMinecraftStatus;
import com.karelmikie3.craftcord.config.Config;
import com.karelmikie3.craftcord.util.ColorHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.minecraft.util.text.TextFormatting.*;

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

    private final List<IMinecraftPresence> presences = new LinkedList<>();
    private final List<IMinecraftStatus> statuses = new LinkedList<>();

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.webhookStatus = DiscordSetupStatus.STARTING;
        this.botStatus = DiscordSetupStatus.STARTING;
        this.discordEvents = new DiscordEvents();

        this.discordEvents = new DiscordEvents();

        try {
            this.webhookClient = new WebhookClientBuilder(Config.getWebhookURL()).build();
        } catch (IllegalArgumentException e) {
            this.webhookStatus = DiscordSetupStatus.INVALID_WEBHOOK_URL;
            LOGGER.warn("Invalid webhook URL", e);
        }

        if (this.webhookStatus != DiscordSetupStatus.INVALID_WEBHOOK_URL)
            this.getWebhookData();

        try {
            this.bot = new JDABuilder(AccountType.BOT)
                    .setToken(Config.getBotToken())
                    .build();

            this.bot.addEventListener(discordEvents);
        } catch (LoginException e) {
            LOGGER.warn("Invalid bot authentication token.", e);
            botStatus = DiscordSetupStatus.INVALID_BOT_TOKEN;
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

        //TODO: do this for config load/reload events.
        setPresence(new ArrayList<>(Config.getPresenceList()));
        setStatus(new ArrayList<>(Config.getStatusList()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerStopping(FMLServerStoppingEvent event) {
        presences.clear();
        statuses.clear();
        updatePresence(event.getServer());

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

    private long counter = 0;
    //private static long TICK_AMOUNT = Config.getPresenceUpdateRate() * 20;
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer() ) {
            if (counter % (Config.getPresenceStatusUpdateRate() * 20) == 0) {
                updatePresence(ServerLifecycleHooks.getCurrentServer());
                updateStatus(ServerLifecycleHooks.getCurrentServer());
            }
            ++counter;
        }
    }

    private void getWebhookData() {
        HttpClient client = HttpClientBuilder.create().build();

        try {
            HttpGet getRequest = new HttpGet(Config.getWebhookURL());
            HttpResponse response = client.execute(getRequest);

            try (InputStream input = response.getEntity().getContent();
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {

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

    public void setPresence(IMinecraftPresence presence) {
        setPresence(Collections.singleton(presence));
    }

    /**
     * Sets the internal {@link List} of Presences call {@link #updatePresence(MinecraftServer)} to update the presence displayed in Discord.
     * @param presences {@link Collection} of {@link IMinecraftPresence} that are displayed in Discord delimited by ', '.
     */
    public void setPresence(Collection<IMinecraftPresence> presences) {
        this.presences.clear();
        this.presences.addAll(presences);
    }

    private String lastPresence = "";
    private void updatePresence(MinecraftServer server) {
        if (!Config.displayPresence())
            return;

        final StringJoiner joiner = new StringJoiner(", ");

        this.presences.stream()
                .map(IMinecraftPresence::getMessage)
                .map(func -> func.apply(server))
                .forEach(s -> {
                    if (joiner.length() + s.length() <= 126) {
                        joiner.add(s);
                    } else {
                        LOGGER.warn("Skipping adding '{}' to the presence, because it results in a presence longer than 126 characters.", s);
                    }
                });
        String presenceString = joiner.toString();

        if (!lastPresence.equals(presenceString)) {
            lastPresence = presenceString;
            if (presenceString.isEmpty()) {
                getBot().getPresence().setGame(null);
            } else {
                getBot().getPresence().setGame(Game.playing(presenceString));
            }
        }
    }

    public void setStatus(IMinecraftStatus status) {
        setStatus(Collections.singleton(status));
    }

    /**
     * Sets the internal {@link List} of Presences call {@link #updateStatus(MinecraftServer)} to update the status displayed in Discord.
     * @param statuses {@link Collection} of {@link IMinecraftStatus} that are displayed in Discord delimited by ', '.
     */
    public void setStatus(Collection<IMinecraftStatus> statuses) {
        this.statuses.clear();
        this.statuses.addAll(statuses);
    }

    private MessageEmbed lastEmbed;
    //TODO: change complete() to queue().
    private void updateStatus(MinecraftServer server) {
        if (!Config.displayStatus() || Config.getStatusChannelID() == 0L)
            return;

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Minecraft status");

        this.statuses.stream()
                .map(status -> new MessageEmbed.Field(status.getTitle().apply(server), status.getMessage().apply(server), true))
                .forEach(builder::addField);

        MessageEmbed embed = builder.build();
        if (Objects.equals(embed, lastEmbed))
            return;

        TextChannel channel = getBot().getTextChannelById(Config.getStatusChannelID());
        if (!channel.canTalk()) {
            LOGGER.warn("Can't talk in designated status channel.");
            //TODO: notify OPs in game.
            return;
        }
        if (Config.getStatusMessageID() == 0L) {
            LOGGER.info("Status message ID '0' assuming config wasn't set. Posting a message myself.");

            Message message = channel.sendMessage(embed).complete();

            Config.setStatusMessageID(message.getIdLong());
            lastEmbed = embed;
        } else {
            try {
                channel.editMessageById(Config.getStatusMessageID(), embed).complete();
                lastEmbed = embed;
            } catch (ErrorResponseException e) {
                switch (e.getErrorResponse()) {
                    case INVALID_AUTHOR_EDIT:
                    case MISSING_ACCESS:
                    case UNKNOWN_MESSAGE:
                        Config.setStatusMessageID(0L);
                        LOGGER.warn("No access to message, posting a new one.");
                        LOGGER.debug(e);
                        break;
                    default:
                        LOGGER.error("Other erroneous response please report.", e);
                        break;
                }
            }
        }

    }

    private class DiscordEvents extends ListenerAdapter {
        private final MinecraftServer SERVER = ServerLifecycleHooks.getCurrentServer();

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (!Config.broadcastDiscordChat())
                return;

            if ((!event.isWebhookMessage() || event.getAuthor().getIdLong() != DiscordHandler.this.webhookID) &&
                    (!Config.sameChannel() || event.getChannel().getIdLong() == channelID)) {

                String message = event.getMessage().getContentDisplay();

                for (Emote emote : event.getMessage().getEmotes()) {
                    CommonEmoteHelper.addToLocalEmoteCache(emote);
                    message = message.replace(":" + emote.getName() + ":", ":" + emote.getId() + ":");
                }
                ITextComponent messengerName = new StringTextComponent(event.getMember().getEffectiveName())
                        .applyTextStyle(ColorHelper.getNearestColor(event.getMember().getColor()));

                ITextComponent chatMessage = new TranslationTextComponent("chat.type.discordText", messengerName, message);

                //If mod language files load on server change to translation.
                SERVER.sendMessage(new StringTextComponent(BLUE + "[" + DARK_BLUE + "DISCORD" + BLUE + "]" + RESET + "<").appendSibling(messengerName).appendText("> ").appendText(message));
                SERVER.getPlayerList().sendPacketToAllPlayers(new SChatPacket(chatMessage, ChatType.CHAT));
            }
        }

        @Override
        public void onMessageDelete(MessageDeleteEvent event) {
            if (Config.getStatusMessageID() == event.getMessageIdLong()) {
                DiscordHandler.this.lastEmbed = null;
                Config.setStatusMessageID(0L);
            }
        }
    }
}
