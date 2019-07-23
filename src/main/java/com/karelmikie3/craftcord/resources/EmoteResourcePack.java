package com.karelmikie3.craftcord.resources;

import com.google.common.collect.Sets;
import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.karelmikie3.craftcord.util.GifUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import static com.karelmikie3.craftcord.util.GifUtil.merge;
import static com.karelmikie3.craftcord.util.GifUtil.readGIF;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EmoteResourcePack extends ResourcePack {

    public EmoteResourcePack() {
        super(new File("dummy"));
    }

    @Override
    protected InputStream getInputStream(String resourcePath) throws IOException {
        String usefulResourcePath = resourcePath.replace("assets/craftcord/textures/emotedata/", "").replaceAll("\\.[^.]*$", "");
        //System.out.println(resourcePath);

        boolean animated = ClientEmoteHelper.isAnimated(usefulResourcePath);

        if (animated && !resourcePath.endsWith(".mcmeta")) {
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(ClientEmoteHelper.getEmoteData(usefulResourcePath))));

            GifUtil.ImageFrame[] frames = readGIF(reader);
            BufferedImage finalImage = null;
            for (GifUtil.ImageFrame frame : frames) {
                finalImage = merge(finalImage, frame.getImage());

            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writer.setOutput(ImageIO.createImageOutputStream(outputStream));
            writer.write(finalImage);

            return new ByteArrayInputStream(outputStream.toByteArray());


        } /*else*/ if (/*animated &&*/ resourcePath.endsWith(".mcmeta")) {

            int frameAmount = 1;
            int height = -1;

            if (animated) {
                ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(ClientEmoteHelper.getEmoteData(usefulResourcePath))));
                GifUtil.ImageFrame[] frames = readGIF(reader);
                frameAmount = frames.length;
                for (GifUtil.ImageFrame frame : frames) {
                    if (frame.getHeight() > height) {
                        if (height != -1) {
                            System.err.println("multiple heights in one emote.");
                        }

                        height = frame.getHeight();
                    }
                }
            }
            //System.out.println("asdf");

            //return new ReaderInputStream(new StringReader("{\"test\":{\"frameAmount\": " + frames.length + ",\"height\": " + maxHeight + "}}"), "UTF-8");
            return new ByteArrayInputStream(("{\"test\":{\"frameAmount\":" + frameAmount + ",\"height\":" + height + ",\"animated\":" + animated + "}}").getBytes());
            //return new FileInputStream("D:\\Development\\IdeaProjects\\1.14\\discraft\\test.mcmeta");
        }




        return new ByteArrayInputStream(ClientEmoteHelper.getEmoteData(usefulResourcePath));
    }

    @Override
    protected boolean resourceExists(String resourcePath) {
        String usefulResourcePath = resourcePath.replace("assets/craftcord/textures/emotedata/", "").replaceAll("\\.[^.]*$", "");

        boolean animated = ClientEmoteHelper.isAnimated(usefulResourcePath);

        if (animated) {
            return true;
        }

        if (/*resourcePath.endsWith(".mcmeta") || */resourcePath.endsWith(".lang"))
            return false;
        return ClientEmoteHelper.hasEmoteData(resourcePath.replace("assets/craftcord/textures/emotedata/", "").replaceAll("\\.[^.]*$", ""));
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String pathIn, int maxDepth, Predicate<String> filter) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getResourceNamespaces(ResourcePackType type) {
        return Sets.newHashSet("craftcord");
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Nullable
    @Override
    public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException {
        return null;
    }
}
