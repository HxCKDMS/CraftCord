package com.karelmikie3.craftcord.util;

import net.dv8tion.jda.core.entities.Emote;

import java.util.*;
import java.util.function.Predicate;

public class CommonEmoteHelper {
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
            } else if ((c >= 'a' && 'z' >= c || c >= 'A' && 'Z' >= c || c >= '0' && '9' >= c || c == '-') && buildingEmote) {
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
        /*nameURLMap = emotes
                .stream()
                .map(emote -> new AbstractMap.SimpleEntry<>(emote.getName(), emote.getImageUrl()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));*/

        serverEmotes = new HashSet<>(emotes);
    }

    public static void removeServerEmotes() {
        /*if (nameURLMap != null)
            nameURLMap.clear();*/

        if (serverEmotes != null)
            serverEmotes.clear();
    }

    public static Set<Emote> getServerEmotes() {
        //return nameURLMap == null ? new HashMap<>() : Collections.unmodifiableMap(nameURLMap);

        return serverEmotes == null ? new HashSet<>() : Collections.unmodifiableSet(serverEmotes);
    }

    public static void addToLocalEmoteCache(Emote emote) {
        emoteCache.put(emote.getIdLong(), emote);
    }

    public static Emote requestFromLocalEmoteCache(long id) {
        return emoteCache.get(id);
    }

    public static void clearLocalEmoteCache(long id) {
        emoteCache.clear();
    }
}
