package com.karelmikie3.craftcord.util;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@OnlyIn(Dist.CLIENT)
public final class ClientEmoteHelper {
    //CLIENT SIDE ONLY STUFF

    @OnlyIn(Dist.CLIENT)
    private static final Map<String, String> displayToIDMap = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static final Map<String, byte[]> emoteData = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static final Set<String> usableEmotes = ConcurrentHashMap.newKeySet();

    @OnlyIn(Dist.CLIENT)
    private static ExecutorService emoteDownloader = Executors.newCachedThreadPool();

    @OnlyIn(Dist.CLIENT)
    public static void addEmote(String URL, String displayName, boolean usable) throws IOException {
        addEmote(new URL(URL), displayName, usable);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addEmote(URL URL, String displayName, boolean usable) {
        emoteDownloader.submit(() -> {
            try {
                downloadEmote(URL, displayName, usable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void downloadEmote(URL url, String displayName, boolean usable) throws IOException {
        String emoteID = url.toString().substring(34).replaceAll("\\.[^.]*$", "");

        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Minecraft CraftCord mod");

        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            int b;
            while ((b = inputStream.read()) != -1)
                outputStream.write(b);

            emoteData.put(emoteID, outputStream.toByteArray());
        }

        displayToIDMap.put(displayName, emoteID);

        if (usable)
            usableEmotes.add(displayName);
    }

    @OnlyIn(Dist.CLIENT)
    public static byte[] getEmoteData(String emoteID) {
        return emoteData.getOrDefault(emoteID, null);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean hasEmoteData(String emoteID) {
        return emoteData.containsKey(emoteID);
    }

    @OnlyIn(Dist.CLIENT)
    public static String getEmoteID(String displayName) {
        return displayToIDMap.get(displayName);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean hasEmote(String displayName) {
        return displayToIDMap.containsKey(displayName);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean hasEmoteID(String emoteID) {
        return displayToIDMap.containsValue(emoteID);
    }

    @OnlyIn(Dist.CLIENT)
    public static Collection<String> getUsableEmotes() {
        return Collections.unmodifiableSet(usableEmotes);
    }

    @OnlyIn(Dist.CLIENT)
    private static Set<Long> requested = new HashSet<>();

    @OnlyIn(Dist.CLIENT)
    private static Set<String> ignore = new HashSet<>();

    @OnlyIn(Dist.CLIENT)
    public static void requestEmote(String emoteID) {
        if (ignore.contains(emoteID))
            return;

        try {
            long id = Long.parseLong(emoteID);

            if (!requested.contains(id)) {
                requested.add(id);

                CraftCord.NETWORK.sendToServer(new RequestEmoteMessageC2S(id));
            }
        } catch (NumberFormatException e) {
            ignore.add(emoteID);
        }
    }
}
