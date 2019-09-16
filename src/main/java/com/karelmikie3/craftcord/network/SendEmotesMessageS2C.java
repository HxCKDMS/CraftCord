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

package com.karelmikie3.craftcord.network;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.entities.Emote;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SendEmotesMessageS2C {
    private final Map<String, String> nameURLMap = new HashMap<>();
    private final Set<String> usableEmotes = new HashSet<>();
    private final Set<String> animatedEmotes = new HashSet<>();

    SendEmotesMessageS2C(Emote emote, boolean usable) {
        nameURLMap.put(emote.getName(), emote.getImageUrl());
        if (usable) {
            usableEmotes.add(emote.getName());
        }

        if (emote.isAnimated()) {
            animatedEmotes.add(emote.getName());
        }
    }

    @SuppressWarnings("unused")
    public SendEmotesMessageS2C(Map<Emote, Boolean> emotes) {
        emotes.forEach((emote, usable) -> {
            nameURLMap.put(emote.getName(), emote.getImageUrl());
            if (usable) {
                usableEmotes.add(emote.getName());
            }

            if (emote.isAnimated()) {
                animatedEmotes.add(emote.getName());
            }
        });
    }

    public SendEmotesMessageS2C(boolean initialize) {
        if (initialize) {
            CommonEmoteHelper.getServerEmotes().forEach((emote) -> {
                nameURLMap.put(emote.getName(), emote.getImageUrl());
                usableEmotes.add(emote.getName());

                if (emote.isAnimated()) {
                    animatedEmotes.add(emote.getName());
                }
            });
        }
    }

    public static void encode(SendEmotesMessageS2C msg, PacketBuffer buffer) {
        buffer.writeInt(msg.nameURLMap.size());
        for (Map.Entry<String, String> entry : msg.nameURLMap.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeString(entry.getValue());
            buffer.writeBoolean(msg.usableEmotes.contains(entry.getKey()));
            buffer.writeBoolean(msg.animatedEmotes.contains(entry.getKey()));
        }
    }

    public static SendEmotesMessageS2C decode(PacketBuffer buffer) {
        SendEmotesMessageS2C msg = new SendEmotesMessageS2C(false);
        int size = buffer.readInt();

        for (int i = 0; i < size; i++) {
            String emoteName = buffer.readString();
            String emoteID = buffer.readString();
            boolean usable = buffer.readBoolean();
            boolean animated = buffer.readBoolean();

            msg.nameURLMap.put(emoteName, emoteID);

            if (usable) {
                msg.usableEmotes.add(emoteName);
            }

            if (animated) {
                msg.animatedEmotes.add(emoteName);
            }
        }

        return msg;
    }

    public static void handle(SendEmotesMessageS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (Map.Entry<String, String> entry : msg.nameURLMap.entrySet()) {
                try {
                    String emoteIDString = entry.getValue().toString().substring(34).replaceAll("\\.[^.]*$", "");
                    long emoteID = Long.parseLong(emoteIDString);

                    CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.prepare(emoteID, entry.getKey(), msg.usableEmotes.contains(entry.getKey()), msg.animatedEmotes.contains(entry.getKey()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
