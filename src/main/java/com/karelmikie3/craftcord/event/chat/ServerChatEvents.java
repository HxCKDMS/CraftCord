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

package com.karelmikie3.craftcord.event.chat;

import com.karelmikie3.craftcord.config.Config;
import com.karelmikie3.craftcord.discord.DiscordHandler;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import com.karelmikie3.craftcord.util.DiscordHelper;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerChatEvents {
    private final DiscordHandler handler;

    public ServerChatEvents(DiscordHandler handler) {
        this.handler = handler;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChat(ServerChatEvent event) {
        if (Config.broadcastMinecraftChat()) {
            String message = event.getMessage();

            List<String> emoteIDs = CommonEmoteHelper.getOrderedEmotes(message, StringUtils::isNumeric);
            emoteIDs = emoteIDs.parallelStream()
                    .filter(emoteID -> handler.getBot().getEmoteById(emoteID) != null)
                    .collect(Collectors.toCollection(LinkedList::new));

            LinkedList<String> emotes = emoteIDs.parallelStream()
                    .map(handler.getBot()::getEmoteById)
                    .map(Emote::getAsMention)
                    .collect(Collectors.toCollection(LinkedList::new));

            for (String id : emoteIDs) {
                message = message.replaceFirst(":" + id + ":", emotes.removeFirst());
            }

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getUsername());
            builder.setContent(DiscordHelper.stripFormattingForDiscord(message));
            builder.setAvatarUrl("https://crafatar.com/avatars/" + event.getPlayer().getGameProfile().getId().toString());
            handler.getWebhookClient().send(builder.build());
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (Config.broadcastDeath() && event.getEntityLiving() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            ITextComponent deathMessage = player.getCombatTracker().getDeathMessage();

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Player death");
            builder.setContent(DiscordHelper.stripFormattingForDiscord(deathMessage.getFormattedText()));
            handler.getWebhookClient().send(builder.build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerAdvancement(AdvancementEvent event) {
        Advancement advancement = event.getAdvancement();

        if (Config.broadcastAdvancement() && advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() &&
                event.getPlayer().world.getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS)) {

            ITextComponent advancementMessage = new TranslationTextComponent(
                    "chat.type.advancement." + advancement.getDisplay().getFrame().getName(),
                    event.getPlayer().getDisplayName(),
                    advancement.getDisplayText().getFormattedText());


            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Player advancement");
            builder.setContent(DiscordHelper.stripFormattingForDiscord(advancementMessage.getFormattedText()));
            handler.getWebhookClient().send(builder.build());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerStarted(FMLServerStartedEvent event) {
        if (Config.broadcastServerStartStop() && handler.getWebhookStatus().isUsable()) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getServer() instanceof DedicatedServer ? "Server status" : "Singleplayer status");
            builder.setContent(event.getServer() instanceof DedicatedServer ? "Server has started." : "Singleplayer world has started.");
            handler.getWebhookClient().send(builder.build());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (Config.broadcastServerStartStop() && handler.getWebhookStatus().isUsable()) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getServer() instanceof DedicatedServer ? "Server status" : "Singleplayer status");
            builder.setContent(event.getServer() instanceof DedicatedServer ? "Server has stopped." : "Singleplayer world has stopped.");
            handler.getWebhookClient().send(builder.build());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Config.broadcastPlayerJoinLeave() && handler.getWebhookStatus().isUsable()) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getPlayer().getServer() instanceof DedicatedServer ? "Server status" : "Singleplayer status");
            builder.setContent(event.getPlayer().getDisplayName().getFormattedText() + " joined the game");
            builder.setAvatarUrl("https://crafatar.com/avatars/" + event.getPlayer().getGameProfile().getId().toString());
            handler.getWebhookClient().send(builder.build());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (Config.broadcastPlayerJoinLeave() && handler.getWebhookStatus().isUsable()) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(event.getPlayer().getServer() instanceof DedicatedServer ? "Server status" : "Singleplayer status");
            builder.setContent(event.getPlayer().getDisplayName().getFormattedText() + " left the game");
            builder.setAvatarUrl("https://crafatar.com/avatars/" + event.getPlayer().getGameProfile().getId().toString());
            handler.getWebhookClient().send(builder.build());
        }
    }
}
