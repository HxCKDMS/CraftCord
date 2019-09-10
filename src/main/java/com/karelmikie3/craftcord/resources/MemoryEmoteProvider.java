package com.karelmikie3.craftcord.resources;

import com.karelmikie3.craftcord.api.emotes.IEmoteProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class MemoryEmoteProvider implements IEmoteProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<Long, byte[]> emotes = new HashMap<>();
    private final Map<Long, byte[]> emoteMetadata = new HashMap<>();
    //https://cdn.discordapp.com/emojis/537722102954983466.png
    private final String URLTemplate;

    public MemoryEmoteProvider(String URLTemplate) {
        this.URLTemplate = URLTemplate;
    }

    public MemoryEmoteProvider() {
        this.URLTemplate = "https://cdn.discordapp.com/emojis/%i.%s";
    }

    @Override
    public void prepare(long emoteid, boolean animated) {
        String urlString = String.format(this.URLTemplate, emoteid, animated ? "gif" : "png");
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.fatal("Emote download URL invalid: " + urlString);
            LOGGER.debug(e);
            return;
        }

        byte[] emoteData;
        try {
            emoteData = download(url);
        } catch (IOException e) {
            LOGGER.error("Exception on downloading emote: " + e.getLocalizedMessage());
            LOGGER.debug(e);
            return;
        }


    }

    private byte[] download(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Minecraft CraftCord mod");

        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int b;
            while ((b = input.read()) != -1)
                output.write(b);

            return output.toByteArray();
        }
    }

    @Override
    public InputStream getInput(long emoteid) {
        return null;
    }

    @Override
    public boolean exists(long emoteid) {
        return false;
    }
}
