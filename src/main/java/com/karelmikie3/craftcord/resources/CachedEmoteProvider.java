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

import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class CachedEmoteProvider extends AbstractEmoteProvider {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Object LOCK = new Object();

    private static final File CACHE_LOCATION = new File("assets/craftcordemotes/");
    private static final File EMOTE_LOCATION = new File(CACHE_LOCATION, "emotes");
    private static final File META_LOCATION = new File(CACHE_LOCATION, "metadata");

    //TODO: clear this set on logout of client.
    private final Set<Long> available = new HashSet<>();

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

    protected void prepareASync(long emoteID, String displayName, boolean usable, boolean animated) {
        File emoteFile = new File(EMOTE_LOCATION, emoteID + ".png");
        File metadataFile = new File(META_LOCATION, emoteID + ".mcmeta");

        if (!emoteFile.exists() || !metadataFile.exists()) {
            String urlString = String.format(this.URLTemplate, emoteID, animated ? "gif" : "png");

            Pair<byte[], byte[]> dataMetadataPair = processEmote(urlString, animated);

            if (dataMetadataPair == null)
                return;

            try {
                Files.write(emoteFile.toPath(), dataMetadataPair.getKey());
                Files.write(metadataFile.toPath(), dataMetadataPair.getValue());
            } catch (IOException e) {
                LOGGER.fatal("Unable to write cache files.");
                LOGGER.debug(e);
            }
            LOGGER.debug("Downloaded emote.");
        } else {
            LOGGER.debug("Loaded emote from cache.");
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
            mc.getTextureManager().loadTexture(emoteResource, new EmoteTexture(emoteResource));
        });
    }

    @Override
    public InputStream getInput(long emoteID, boolean metadata) {
        try {
            return new FileInputStream(new File(metadata ? META_LOCATION : EMOTE_LOCATION, emoteID + (metadata ? ".mcmeta" : ".png")));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No such emote exists.");
        }
    }

    @Override
    public boolean exists(long emoteID) {
        return available.contains(emoteID);
    }
}
