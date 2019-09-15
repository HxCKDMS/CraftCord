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

package com.karelmikie3.craftcord.util;

import net.dv8tion.jda.core.entities.Emote;

import java.util.*;
import java.util.function.Predicate;

public final class CommonEmoteHelper {
    private static final HashMap<Long, Emote> emoteCache = new HashMap<>();
    //private static Map<String, String> nameURLMap;
    private static Set<Emote> serverEmotes;

    public static List<String> getOrderedEmotes(String text, Predicate<String> emoteCheck) {
        List<String> emotes = new LinkedList<>();

        StringBuilder emoteBuilder = new StringBuilder();
        boolean buildingEmote = false;

        for (char c : text.toCharArray()) {
            if (c == ':' && !buildingEmote) {
                buildingEmote = true;
            } else if ((c >= 'a' && 'z' >= c || c >= 'A' && 'Z' >= c || c >= '0' && '9' >= c || c == '-' || c == '_') && buildingEmote) {
                emoteBuilder.append(c);
            } else if (c == ':' && emoteBuilder.length() > 0) {
                buildingEmote = false;
                if (emoteCheck.test(emoteBuilder.toString())) {
                    emotes.add(emoteBuilder.toString());
                }
                emoteBuilder = new StringBuilder();
            }
        }

        return emotes;
    }

    public static void setServerEmotes(List<Emote> emotes) {
        serverEmotes = new HashSet<>(emotes);
    }

    public static void clearServerEmotes() {
        if (serverEmotes != null)
            serverEmotes.clear();
    }

    public static Set<Emote> getServerEmotes() {
        return serverEmotes == null ? new HashSet<>() : Collections.unmodifiableSet(serverEmotes);
    }

    public static void addToLocalEmoteCache(Emote emote) {
        emoteCache.put(emote.getIdLong(), emote);
    }

    public static Emote requestFromLocalEmoteCache(long id) {
        return emoteCache.get(id);
    }

    public static void clearLocalEmoteCache() {
        emoteCache.clear();
    }
}
