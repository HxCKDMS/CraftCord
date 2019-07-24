package com.karelmikie3.craftcord.util;

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
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.*;

@OnlyIn(Dist.CLIENT)
public final class ClientEmoteHelper {
    private static final Minecraft mc = Minecraft.getInstance();

    @OnlyIn(Dist.CLIENT)
    private static final Map<String, String> displayToIDMap = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static final Map<String, byte[]> emoteData = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static final Set<String> usableEmotes = ConcurrentHashMap.newKeySet();

    @OnlyIn(Dist.CLIENT)
    private static final Map<String, byte[]> emoteMetadata = new ConcurrentHashMap<>();

    @OnlyIn(Dist.CLIENT)
    private static ExecutorService emoteDownloader = Executors.newFixedThreadPool(2);

    @OnlyIn(Dist.CLIENT)
    public static void addEmote(String URL, String displayName, boolean usable, boolean animated) throws IOException {
        addEmote(new URL(URL), displayName, usable, animated);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addEmote(URL URL, String displayName, boolean usable, boolean animated) {
        emoteDownloader.submit(() -> {
            try {
                downloadEmote(URL, displayName, usable, animated);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
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

        StringBuilder metadataBuilder = new StringBuilder("{\"emote\":{\"delays\":[");
        int frameAmount = -1;
        int height = -1;

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

                    if (frame.getHeight() > height) {
                        if (height != -1) {
                            System.err.println("multiple heights in one emote.");
                        }

                        height = frame.getHeight();
                    }

                    metadataBuilder.append(frame.getDelay()).append(',');
                }

                writer.write(finalImage);
                metadataBuilder.deleteCharAt(metadataBuilder.lastIndexOf(","));

                data = output.toByteArray();
            }
        }
        metadataBuilder.append("],\"height\":").append(height)
                       .append(",\"frameAmount\":").append(frameAmount)
                       .append(",\"animated\":").append(animated)
                       .append("}}");

        if (data != null) {
            emoteData.put(emoteID, data);
            emoteMetadata.put(emoteID, metadataBuilder.toString().getBytes());
            displayToIDMap.put(displayName, emoteID);

            if (usable)
                usableEmotes.add(displayName);

            ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);

            executor.runImmediately(() -> {
                ResourceLocation emote = new ResourceLocation("craftcord", "textures/emotedata/" + emoteID);
                mc.getTextureManager().loadTickableTexture(emote, new EmoteTexture(emote));
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static byte[] getEmoteMetadata(String emoteID) {
        return emoteMetadata.getOrDefault(emoteID, null);
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
        System.out.println("usableEmotes.size() = " + usableEmotes.size());
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
