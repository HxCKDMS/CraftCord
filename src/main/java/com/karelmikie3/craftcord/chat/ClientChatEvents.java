package com.karelmikie3.craftcord.chat;

import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ClientChatEvents {

    /*@SubscribeEvent
    public static void renderChat(RenderGameOverlayEvent.Chat event) {

    }*/

    @SubscribeEvent
    public void clientChatEvent(ClientChatEvent event) {
        String message = event.getMessage();

        List<String> emotes = CommonEmoteHelper.getOrderedEmotes(message, ClientEmoteHelper::hasEmote);
        LinkedList<String> emoteIds = emotes.parallelStream().map(ClientEmoteHelper::getEmoteID).collect(Collectors.toCollection(LinkedList::new));

        for (String emote : emotes) {
            message = message.replaceFirst(":" + emote + ":", ":" + emoteIds.removeFirst() + ":");
        }

        event.setMessage(message);
    }
}
