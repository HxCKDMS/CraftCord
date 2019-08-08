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

package com.karelmikie3.craftcord.chat;

import com.karelmikie3.craftcord.discord.DiscordHandler;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerChatEvents {
    private final WebhookClient webhookClient;
    private final JDA bot;

    public ServerChatEvents(DiscordHandler handler) {
        this.webhookClient = handler.getWebhookClient();
        this.bot = handler.getBot();
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        String message = event.getMessage();

        List<String> emoteIDs = CommonEmoteHelper.getOrderedEmotes(message, StringUtils::isNumeric);
        LinkedList<String> emotes = emoteIDs.parallelStream()
                .map(bot::getEmoteById)
                .map(Emote::getAsMention)
                .collect(Collectors.toCollection(LinkedList::new));

        for (String id : emoteIDs) {
            message = message.replaceFirst(":" + id + ":", emotes.removeFirst());
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(event.getUsername());
        builder.setContent(message);
        builder.setAvatarUrl("https://crafatar.com/avatars/" + event.getPlayer().getGameProfile().getId().toString());
        webhookClient.send(builder.build());
    }


    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            ITextComponent deathMessage = player.getCombatTracker().getDeathMessage();

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Player death");
            builder.setContent(deathMessage.getFormattedText());
            webhookClient.send(builder.build());
        }
    }
}
