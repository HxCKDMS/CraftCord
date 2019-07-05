package com.karelmikie3.craftcord.chat;

import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerChatEvents {
    private final WebhookClient webhookClient;
    private final JDA bot;

    public ServerChatEvents(WebhookClient webhookClient, JDA bot) {
        this.webhookClient = webhookClient;
        this.bot = bot;
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        String message = event.getMessage();

        List<String> emoteIDs = CommonEmoteHelper.getOrderedEmotes(message, ClientEmoteHelper::hasEmoteID);
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
}
