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
import com.karelmikie3.craftcord.util.GifUtil;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractEmoteProvider implements IEmoteProvider {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected static final Gson GSON = new GsonBuilder().create();
    private static final ExecutorService emoteProcessor = Executors.newFixedThreadPool(2);

    //TODO: reset on client logout
    protected final Set<Long> requested = new HashSet<>();
    protected final Map<String, Long> displayNameToID = new HashMap<>();
    protected final Set<String> usable = new HashSet<>();

    @Override
    public final void prepare(long emoteID, String displayName, boolean usable, boolean animated) {
        emoteProcessor.submit(() -> prepareASync(emoteID, displayName, usable, animated));
    }

    protected abstract void prepareASync(long emoteID, String displayName, boolean usable, boolean animated);

    @Nullable
    protected Pair<byte[], byte[]> processEmote(String urlString, boolean animated) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LOGGER.fatal("Emote download URL invalid: " + urlString);
            LOGGER.debug(e);
            return null;
        }

        byte[] emoteData = download(url);
        if (emoteData == null)
            return null;

        JsonObject emoteJson = new JsonObject();

        if (animated) {
            AnimatedEmoteData data = processAnimatedEmote(emoteData);
            if (data == null)
                return null;

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
                return null;
        }

        JsonObject metadataJson = new JsonObject();
        metadataJson.add("emote", emoteJson);
        byte[] metadata = GSON.toJson(metadataJson).getBytes(StandardCharsets.UTF_8);

        return new Pair<>(emoteData, metadata);
    }

    @Nullable
    protected byte[] processNormalEmote(byte[] emoteData) {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(emoteData));
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

            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    protected AnimatedEmoteData processAnimatedEmote(byte[] emoteData) {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(emoteData));
             ByteArrayOutputStream output = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {

            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            reader.setInput(imageInput);
            writer.setOutput(imageOutput);

            ArrayList<GifUtil.ImageFrame> frames = GifUtil.readGIF(reader);

            int frameAmount = frames.size();

            OptionalInt maxDimOptional = frames.parallelStream()
                    .mapToInt(GifUtil.ImageFrame::getDim)
                    .max();

            if (!maxDimOptional.isPresent())
                return null;
            int maxDim = maxDimOptional.getAsInt();

            BufferedImage emote = stitchGifEmote(frames, maxDim);
            writer.write(emote);

            List<Integer> delays = frames.stream()
                    .mapToInt(GifUtil.ImageFrame::getDelay)
                    .boxed()
                    .collect(Collectors.toList());

            return new AnimatedEmoteData(output.toByteArray(), frameAmount, delays, maxDim);

        } catch (IOException e) {
            LOGGER.error("Exception processing animated emote: " + e.getLocalizedMessage());
            LOGGER.debug(e);
            return null;
        }
    }

    protected static BufferedImage stitchGifEmote(List<GifUtil.ImageFrame> frames, int dim) {
        BufferedImage combined = new BufferedImage(dim, dim * frames.size(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = combined.getGraphics();

        for (int i = 0; i < frames.size(); i++) {
            GifUtil.ImageFrame frame = frames.get(i);
            graphics.drawImage(frame.getImage(), (dim - frame.getWidth()) >> 1, dim * i + ((dim - frame.getHeight()) >> 1), null);
        }

        return combined;
    }

    @Nullable
    protected byte[] download(URL url) {
        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Minecraft CraftCord mod");

            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                int b;
                while ((b = input.read()) != -1)
                    output.write(b);

                return output.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error("Exception on downloading emote: " + e.getLocalizedMessage());
            LOGGER.debug(e);
            return null;
        }
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

    static class AnimatedEmoteData {

        private final byte[] emoteImage;
        private final int frameAmount;
        private final List<Integer> delays;
        private final int dim;

        AnimatedEmoteData(byte[] emoteImage, int frameAmount, List<Integer> delays, int dim) {

            this.emoteImage = emoteImage;
            this.frameAmount = frameAmount;
            this.delays = delays;
            this.dim = dim;
        }

        byte[] getEmoteImage() {
            return emoteImage;
        }

        int getFrameAmount() {
            return frameAmount;
        }

        List<Integer> getDelays() {
            return delays;
        }

        int getDim() {
            return dim;
        }
    }
}
