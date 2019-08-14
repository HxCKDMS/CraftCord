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

import com.google.gson.*;
import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import com.karelmikie3.craftcord.resources.EmoteTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//TODO: remove excessive concurrency.
@OnlyIn(Dist.CLIENT)
public final class ClientEmoteHelper {
    private static final Minecraft mc = Minecraft.getInstance();

    private static final Map<String, String> displayToIDMap = new ConcurrentHashMap<>();
    
    private static final Map<String, byte[]> emoteData = new ConcurrentHashMap<>();

    private static final Set<String> usableEmotes = ConcurrentHashMap.newKeySet();

    private static final Map<String, byte[]> emoteMetadata = new ConcurrentHashMap<>();

    private static final ExecutorService emoteDownloader = Executors.newFixedThreadPool(2);

    private static final Gson GSON = new GsonBuilder().create();
    
    public static void addEmote(String URL, String displayName, boolean usable, boolean animated) throws IOException {
        addEmote(new URL(URL), displayName, usable, animated);
    }

    private static void addEmote(URL URL, String displayName, boolean usable, boolean animated) {
        emoteDownloader.submit(() -> {
            try {
                downloadEmote(URL, displayName, usable, animated);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //TODO: method is way too big. Split into different methods.
    private static void downloadEmote(URL url, String displayName, boolean usable, boolean animated) throws IOException {
        String emoteID = url.toString().substring(34).replaceAll("\\.[^.]*$", "");

        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Minecraft CraftCord mod");

        byte[] data;
        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            int b;
            while ((b = input.read()) != -1)
                output.write(b);


            data = output.toByteArray();
        }
        JsonObject emoteJson = new JsonObject();
        JsonArray delaysJson = new JsonArray();

        int frameAmount = -1;
        int frameHeight = -1;

        if (animated && data != null) {
            try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
                 ByteArrayOutputStream output = new ByteArrayOutputStream();
                 ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {

                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
                reader.setInput(imageInput);
                writer.setOutput(imageOutput);

                GifUtil.ImageFrame[] frames = GifUtil.readGIF(reader);
                frameAmount = frames.length;

                BufferedImage finalImage = null;
                for (GifUtil.ImageFrame frame : frames) {
                    finalImage = GifUtil.merge(finalImage, frame.getImage());

                    if (frame.getHeight() > frameHeight) {
                        if (frameHeight != -1) {
                            System.err.println("multiple heights in one emote.");
                        }

                        frameHeight = frame.getHeight();
                    }

                    delaysJson.add(frame.getDelay());
                }

                if (finalImage != null)
                    writer.write(finalImage);

                data = output.toByteArray();
            }
        } else if (data != null) {
            try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
                 ByteArrayOutputStream output = new ByteArrayOutputStream();
                 ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {

                ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
                ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

                reader.setInput(imageInput);
                writer.setOutput(imageOutput);

                BufferedImage inputImage = reader.read(0);

                final int height = inputImage.getHeight();
                final int width = inputImage.getWidth();
                final int dim = Math.max(height, width);

                BufferedImage squaredImage = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);

                Graphics graphics = squaredImage.getGraphics();

                graphics.drawImage(inputImage, (dim - width)>>1, (dim - height)>>1, null);
                writer.write(squaredImage);

                data = output.toByteArray();
            }
        }


        emoteJson.add("delays", delaysJson);
        emoteJson.add("height", new JsonPrimitive(frameHeight));
        emoteJson.add("frameAmount", new JsonPrimitive(frameAmount));
        emoteJson.add("animated", new JsonPrimitive(animated));

        if (data != null) {
            JsonObject metadataJson = new JsonObject();
            metadataJson.add("emote", emoteJson);
            byte[] metadata = GSON.toJson(metadataJson).getBytes(StandardCharsets.UTF_8);

            emoteData.put(emoteID, data);
            emoteMetadata.put(emoteID, metadata);
            displayToIDMap.put(displayName, emoteID);

            if (usable)
                usableEmotes.add(displayName);

            ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);

            executor.runImmediately(() -> {
                ResourceLocation emoteResource = new ResourceLocation("craftcordemotes", "textures/emotedata/" + emoteID);
                mc.getTextureManager().loadTickableTexture(emoteResource, new EmoteTexture(emoteResource));
            });
        }
    }
    
    public static byte[] getEmoteMetadata(String emoteID) {
        return emoteMetadata.getOrDefault(emoteID, null);
    }

    public static byte[] getEmoteData(String emoteID) {
        return emoteData.getOrDefault(emoteID, null);
    }
    
    public static boolean hasEmoteData(String emoteID) {
        return emoteData.containsKey(emoteID);
    }

    public static String getEmoteID(String displayName) {
        return displayToIDMap.get(displayName);
    }

    public static boolean hasEmote(String displayName) {
        return displayToIDMap.containsKey(displayName);
    }

    public static boolean hasEmoteID(String emoteID) {
        return displayToIDMap.containsValue(emoteID);
    }

    @SuppressWarnings("unused")
    public static Collection<String> getAllEmoteIDs() {
        return Collections.unmodifiableSet(emoteData.keySet());
    }

    public static Collection<String> getUsableEmotes() {
        return Collections.unmodifiableSet(usableEmotes);
    }
    
    private static final Set<Long> requested = new HashSet<>();
    
    private static final Set<String> ignore = new HashSet<>();

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
