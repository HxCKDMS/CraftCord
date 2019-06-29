package com.karelmikie3.craftcord.event;

import com.karelmikie3.craftcord.util.ColorHelper;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class DiscordMessageEvent extends ListenerAdapter {
    private final MinecraftServer server;

    public DiscordMessageEvent(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if  (!event.isWebhookMessage()) {
            ITextComponent component = new StringTextComponent("[DISCORD]<")
                    .appendSibling(new StringTextComponent(event.getMember().getEffectiveName()).applyTextStyle(ColorHelper.getNearestColor(event.getMember().getColor())))
                    .appendSibling(new StringTextComponent("> "))
                    .appendSibling(new StringTextComponent(event.getMessage().getContentDisplay()));

            server.getPlayerList().sendMessage(component);
        }
    }
}
