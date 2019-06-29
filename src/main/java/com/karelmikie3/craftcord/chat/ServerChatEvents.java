package com.karelmikie3.craftcord.chat;

import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerChatEvents {
    private final WebhookClient webhookClient;

    public ServerChatEvents(WebhookClient webhookClient) {
        this.webhookClient = webhookClient;
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(event.getUsername());
        builder.setContent(event.getMessage());
        builder.setAvatarUrl("https://crafatar.com/avatars/" + event.getPlayer().getGameProfile().getId().toString());
        //webhookClient.send(builder.build());
    }
}
