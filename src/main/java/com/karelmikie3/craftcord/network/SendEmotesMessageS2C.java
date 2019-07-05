package com.karelmikie3.craftcord.network;

import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import net.dv8tion.jda.core.entities.Emote;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SendEmotesMessageS2C {
    private final Map<String, String> nameURLMap = new HashMap<>();
    private final Set<String> usableEmotes = new HashSet<>();

    public SendEmotesMessageS2C(Emote emote, boolean usable) {
        nameURLMap.put(emote.getName(), emote.getImageUrl());
        if (usable) {
            usableEmotes.add(emote.getName());
        }
    }

    public SendEmotesMessageS2C(Map<Emote, Boolean> emotes) {
        emotes.forEach((emote, usable) -> {
            nameURLMap.put(emote.getName(), emote.getImageUrl());
            if (usable) {
                usableEmotes.add(emote.getName());
            }
        });
    }

    public SendEmotesMessageS2C(boolean initialize) {
        if (initialize) {
            CommonEmoteHelper.getServerEmotes().forEach((name, url) -> {
                nameURLMap.put(name, url);
                usableEmotes.add(name);
            });
        }
    }

    public static void encode(SendEmotesMessageS2C msg, PacketBuffer buffer) {
        buffer.writeInt(msg.nameURLMap.size());
        for (Map.Entry<String, String> entry : msg.nameURLMap.entrySet()) {
            buffer.writeString(entry.getKey());
            buffer.writeString(entry.getValue());
            buffer.writeBoolean(msg.usableEmotes.contains(entry.getKey()));
        }
    }

    public static SendEmotesMessageS2C decode(PacketBuffer buffer) {
        SendEmotesMessageS2C msg = new SendEmotesMessageS2C(false);
        int size = buffer.readInt();

        for (int i = 0; i < size; i++) {
            String emoteName = buffer.readString();
            String emoteID = buffer.readString();
            boolean usable = buffer.readBoolean();

            msg.nameURLMap.put(emoteName, emoteID);

            if (usable) {
                msg.usableEmotes.add(emoteName);
            }
        }

        return msg;
    }

    public static void handle(SendEmotesMessageS2C msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (Map.Entry<String, String> entry : msg.nameURLMap.entrySet()) {
                try {
                    ClientEmoteHelper.addEmote(entry.getValue(), entry.getKey(), msg.usableEmotes.contains(entry.getKey()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
