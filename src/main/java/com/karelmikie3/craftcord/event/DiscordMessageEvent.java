package com.karelmikie3.craftcord.event;

import com.karelmikie3.craftcord.util.ColorHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

public class DiscordMessageEvent extends ListenerAdapter {
    private final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    private final UUID sessionUUID;

    public DiscordMessageEvent(UUID sessionUUID) {

        this.sessionUUID = sessionUUID;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isWebhookMessage()) {
            String message = event.getMessage().getContentDisplay();

            for (Emote emote : event.getMessage().getEmotes()) {
                CommonEmoteHelper.addToLocalEmoteCache(emote);
                message = message.replace(":" + emote.getName() + ":", ":" + emote.getId() + ":");
            }

            ITextComponent component = new StringTextComponent("[DISCORD]<")
                    .appendSibling(new StringTextComponent(event.getMember().getEffectiveName()).applyTextStyle(ColorHelper.getNearestColor(event.getMember().getColor())))
                    .appendSibling(new StringTextComponent("> "))
                    .appendSibling(new StringTextComponent(message));

            server.getPlayerList().sendMessage(component);
        } else if (event.getMessage().getContentRaw().equals("Server + Channel check: " + sessionUUID.toString())) {
            System.out.println("Found channel!");
            CommonEmoteHelper.setServerEmotes(event.getGuild().getEmotes());
            event.getMessage().delete().reason("mc channel check msg").queue();
        }
    }
}
