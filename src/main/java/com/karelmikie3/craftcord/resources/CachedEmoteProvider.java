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

package com.karelmikie3.craftcord.resources;

import com.google.gson.*;
import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.api.emotes.IEmoteProvider;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.karelmikie3.craftcord.resources.MemoryEmoteProvider.*;

public class CachedEmoteProvider implements IEmoteProvider {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final Object LOCK = new Object();
    private static final ExecutorService emoteProcessor = Executors.newFixedThreadPool(2);

    private static final File CACHE_LOCATION = new File("assets/craftcordemotes/");
    private static final File EMOTE_LOCATION = new File(CACHE_LOCATION, "emotes");
    private static final File META_LOCATION = new File(CACHE_LOCATION, "metadata");

    //TODO: clear these maps on logout of client.
    private final Set<Long> available = new HashSet<>();
    private final Map<String, Long> displayNameToID = new HashMap<>();
    private final Set<String> usable = new HashSet<>();
    private final Set<Long> requested = new HashSet<>();

    private final String URLTemplate;

    public CachedEmoteProvider(String URLTemplate) {
        this.URLTemplate = URLTemplate;
    }

    public CachedEmoteProvider() {
        this.URLTemplate = "https://cdn.discordapp.com/emojis/%d.%s";
    }

    static {
        if (!EMOTE_LOCATION.exists()) {
            if (!EMOTE_LOCATION.mkdirs()) {
                LOGGER.error("Cannot create emote cache directory.");
                throw new RuntimeException("Cannot create emote directory.");
            }
        }

        if (!META_LOCATION.exists()) {
            if (!META_LOCATION.mkdirs()) {
                LOGGER.error("Cannot create emote metadata cache directory.");
                throw new RuntimeException("Cannot create emote metadata directory.");
            }
        }
    }

    @Override
    public void prepare(long emoteID, String displayName, boolean usable, boolean animated) {
        emoteProcessor.submit(() -> prepare0(emoteID, displayName, usable, animated));
    }

    private void prepare0(long emoteID, String displayName, boolean usable, boolean animated) {
        File emoteFile = new File(EMOTE_LOCATION, Long.toString(emoteID));
        File metadataFile = new File(META_LOCATION, Long.toString(emoteID));

        if (!emoteFile.exists() || !metadataFile.exists()) {
            String urlString = String.format(this.URLTemplate, emoteID, animated ? "gif" : "png");
            URL url;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                LOGGER.fatal("Emote download URL invalid: " + urlString);
                LOGGER.debug(e);
                return;
            }

            byte[] emoteData = download(url);
            if (emoteData == null)
                return;

            JsonObject emoteJson = new JsonObject();

            if (animated) {
                AnimatedEmoteData data = processAnimatedEmote(emoteData);
                if (data == null)
                    return;

                emoteData = data.getEmoteImage();
                JsonArray delaysJson = new JsonArray();
                data.getDelays().forEach(delaysJson::add);
                emoteJson.add("delays", delaysJson);
                emoteJson.add("height", new JsonPrimitive(data.getDim()));
                emoteJson.add("frameAmount", new JsonPrimitive(data.getFrameAmount()));
                emoteJson.add("animated", new JsonPrimitive(true));
            } else {
                emoteData = processNormalEmote(emoteData);
                //TODO: make it so this doesn't have to be.
                emoteJson.add("delays", new JsonArray());
                emoteJson.add("height", new JsonPrimitive(-1));
                emoteJson.add("frameAmount", new JsonPrimitive(-1));
                emoteJson.add("animated", new JsonPrimitive(false));

                if (emoteData == null)
                    return;
            }

            JsonObject metadataJson = new JsonObject();
            metadataJson.add("emote", emoteJson);
            byte[] metadata = GSON.toJson(metadataJson).getBytes(StandardCharsets.UTF_8);

            try {
                Files.write(emoteFile.toPath(), emoteData);
                Files.write(metadataFile.toPath(), metadata);
            } catch (IOException e) {
                LOGGER.fatal("Unable to write cache files.");
                LOGGER.debug(e);
            }
            LOGGER.info("Downloaded.");
        } else {
            LOGGER.info("Loaded from cache.");
        }

        synchronized (LOCK) {
            this.available.add(emoteID);
            this.displayNameToID.put(displayName, emoteID);
            if (usable)
                this.usable.add(displayName);
        }

        ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);

        executor.runImmediately(() -> {
            ResourceLocation emoteResource = new ResourceLocation("craftcordemotes", "textures/emotedata/" + emoteID);
            mc.getTextureManager().loadTickableTexture(emoteResource, new EmoteTexture(emoteResource));
        });
    }

    @Override
    public InputStream getInput(long emoteID, boolean metadata) {
        try {
            return new FileInputStream(new File(metadata ? META_LOCATION : EMOTE_LOCATION, Long.toString(emoteID)));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No such emote exists.");
        }
    }

    @Override
    public boolean exists(long emoteID) {
        return available.contains(emoteID);
    }

    @Override
    public boolean exists(String displayName) {
        return displayNameToID.containsKey(displayName);
    }

    @Override
    public long getEmoteID(String displayName) {
        Long id = displayNameToID.get(displayName);
        if (id == null)
            throw new IllegalArgumentException("No such emote exists.");

        return id;
    }

    @Override
    public void requestFromServer(long emoteID) {
        if (!requested.contains(emoteID)) {
            requested.add(emoteID);

            CraftCord.NETWORK.sendToServer(new RequestEmoteMessageC2S(emoteID));
        }
    }

    @Override
    public Set<String> usableEmotes() {
        return Collections.unmodifiableSet(usable);
    }
}
