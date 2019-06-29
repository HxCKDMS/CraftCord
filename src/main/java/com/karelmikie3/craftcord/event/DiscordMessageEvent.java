package com.karelmikie3.craftcord.event;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class DiscordMessageEvent extends ListenerAdapter {
    private final MinecraftServer server;

    public DiscordMessageEvent(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if  (!event.isWebhookMessage()) {
            server.getPlayerList().sendMessage(new StringTextComponent("[DISCORD]<" + event.getMember().getEffectiveName() + ">" + " " + event.getMessage().getContentDisplay()));
        }
    }
}
