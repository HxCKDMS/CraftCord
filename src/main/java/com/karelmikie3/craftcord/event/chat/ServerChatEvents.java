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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
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
import java.util.stream.Collectors;

public class ServerChatEvents {
    private final DiscordHandler handler;

    public ServerChatEvents(DiscordHandler handler) {
        this.handler = handler;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerChat(ServerChatEvent event) {
        String message = event.getMessage();

        LinkedList<String> emoteIDs = new LinkedList<>(CommonEmoteHelper.getOrderedEmotes(message, StringUtils::isNumeric));
        emoteIDs = emoteIDs.parallelStream()
                .filter(emoteID -> handler.getBot().getEmoteById(emoteID) != null)
                .collect(Collectors.toCollection(LinkedList::new));

        TranslationTextComponent component = (TranslationTextComponent) event.getComponent();

        Object[] args = component.getFormatArgs();

        for (int i = 0; i < args.length; i++) {
            if (!(args[i] instanceof ITextComponent))
                continue;

            ITextComponent argComponent = (ITextComponent) args[i];

            LinkedList<String> emotes = new LinkedList<>(CommonEmoteHelper.getOrderedEmotes(argComponent.getFormattedText(), StringUtils::isNumeric));
            emoteIDs = emoteIDs.parallelStream()
                    .filter(emoteID -> handler.getBot().getEmoteById(emoteID) != null)
                    .collect(Collectors.toCollection(LinkedList::new));

            ITextComponent newComponent = null;
            LinkedList<ITextComponent> siblings = new LinkedList<>(argComponent.getSiblings());
            String lastPart = argComponent.getUnformattedComponentText();

            for (String emoteID : emotes) {
                String[] parts = lastPart.split(":" + emoteID + ":", 2);

                if (!parts[0].isEmpty() && newComponent != null)
                    newComponent.appendText(parts[0]);
                else if (!parts[0].isEmpty()) {
                    newComponent = new StringTextComponent(parts[0]);
                }

                ITextComponent emoteComponent = new StringTextComponent("  ");
                emoteComponent.applyTextStyle(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(emoteID))));
                if (newComponent != null) {
                    newComponent.appendSibling(emoteComponent);
                } else {
                    newComponent = emoteComponent;
                }

                lastPart = parts[1];
            }

            if (!lastPart.isEmpty())
                component.appendText(lastPart);

            if (newComponent == null) {
                newComponent = argComponent;
            } else if (!lastPart.isEmpty()) {
                newComponent.appendText(lastPart);

                if (!siblings.isEmpty())
                    siblings.forEach(newComponent::appendSibling);
            }

            args[i] = newComponent;
        }

        ITextComponent newComponent = new TranslationTextComponent(component.getKey(), (Object[]) args);
        event.setComponent(newComponent);

        if (Config.broadcastMinecraftChat()) {


            /*if (DiscordHandler.users.containsKey(event.getPlayer())) {
                String formtext = event.getComponent().getFormattedText().replace(event.getUsername(), DiscordHandler.users.get(event.getPlayer()));
                ITextComponent comp = new StringTextComponent(formtext).setStyle(event.getComponent().getStyle());
                event.setComponent(comp);
            }*/


            LinkedList<String> emotes = emoteIDs.parallelStream()
                    .map(handler.getBot()::getEmoteById)
                    .map(Emote::getAsMention)
                    .collect(Collectors.toCollection(LinkedList::new));

            for (String id : emoteIDs) {
                message = message.replaceFirst(":" + id + ":", emotes.removeFirst());
            }

            String usernm = event.getUsername();
            if (!DiscordHandler.users.isEmpty() && DiscordHandler.users.containsKey(event.getPlayer())) {
                usernm = DiscordHandler.users.get(event.getPlayer());
            }
            if (Config.getGamemodeDisplay()) {
                String gamemode = "";
                switch (event.getPlayer().interactionManager.getGameType().getID()) {
                    case 0: gamemode = "Survival";
                    break;
                    case 1: gamemode = "Creative";
                    break;
                    case 2: gamemode = "Adventure";
                    break;
                    case 3: gamemode = "Spectator";
                    break;
                    case -1: gamemode = "Unknown";
                    break;
                }
                usernm = "[" + gamemode + "] " + usernm;
            }

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(usernm);
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
