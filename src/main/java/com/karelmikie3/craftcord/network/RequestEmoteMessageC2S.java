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

import java.util.function.Supplier;

public class RequestEmoteMessageC2S {
    private final long emoteID;

    public RequestEmoteMessageC2S(long emoteID) {
        this.emoteID = emoteID;
    }

    public static void encode(RequestEmoteMessageC2S msg, PacketBuffer buffer) {
        buffer.writeLong(msg.emoteID);
    }

    public static RequestEmoteMessageC2S decode(PacketBuffer buffer) {
        return new RequestEmoteMessageC2S(buffer.readLong());
    }

    public static void handle(RequestEmoteMessageC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Emote emote = CommonEmoteHelper.requestFromLocalEmoteCache(msg.emoteID);

            if (emote != null) {
                CraftCord.NETWORK.reply(new SendEmotesMessageS2C(emote, CommonEmoteHelper.getServerEmotes().contains(emote)), ctx.get());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
