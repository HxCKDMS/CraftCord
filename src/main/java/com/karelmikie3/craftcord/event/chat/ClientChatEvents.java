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

import com.karelmikie3.craftcord.CraftCord;
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
/*
    @SubscribeEvent
    public void clientHoverEvent(HoverEvent event) {
        if (event.getAction() == HoverEvent.Action.SHOW_TEXT) {
            if (event.getValue().getFormattedText().contains(":")) {
                event.getValue().appendSibling(NewChatGuiPatch.removeEmotes(event.getValue()));
            }
        }
    }*/
        List<String> emotes = CommonEmoteHelper.getOrderedEmotes(message, CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider::exists);
        LinkedList<String> emoteIds = emotes.parallelStream()
                .mapToLong(CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider::getEmoteID)
                .mapToObj(Long::toString)
                .collect(Collectors.toCollection(LinkedList::new));

        for (String emote : emotes) {
            message = message.replaceFirst(":" + emote + ":", ":" + emoteIds.removeFirst() + ":");
        }

        event.setMessage(message);
    }

}
