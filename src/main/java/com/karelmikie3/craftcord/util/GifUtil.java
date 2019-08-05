package com.karelmikie3.craftcord.util;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GifUtil {
    public static void main(String[] args) throws IOException {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        reader.setInput(ImageIO.createImageInputStream(new FileInputStream("peek.gif")));

        ImageFrame[] frames = readGIF(reader);
        BufferedImage finalImage = null;
        int maxHeight = 0;
        int i = 0;
        for (ImageFrame frame : frames) {
            finalImage = merge(finalImage, frame.image);
            writer.setOutput(ImageIO.createImageOutputStream(new FileOutputStream("peek" + i++ + ".png")));
            writer.write(frame.image);

            if (frame.getHeight() > maxHeight) {
                if (maxHeight != 0) {
                    System.err.println("multiple heights in one emote.");
                }

                maxHeight = frame.getHeight();
            }
        }

        writer.setOutput(ImageIO.createImageOutputStream(new FileOutputStream("peekmerged.png")));
        writer.write(finalImage);
    }

    public static BufferedImage merge(BufferedImage image1, BufferedImage image2) {
        if (image1 == null) {
            int dim = Math.max(image2.getHeight(), image2.getWidth());
            BufferedImage resize = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);

            Graphics g = resize.getGraphics();
            g.drawImage(image2, 0, dim - image2.getHeight(), null);

            return resize;
        }

        int w = Math.max(image1.getWidth(), image2.getWidth());
        int h = image1.getHeight() + w;

        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined.getGraphics();
        g.drawImage(image1, 0, 0, null);
        g.drawImage(image2, 0, image1.getHeight() + w - image2.getHeight(), null);

        return combined;
    }

    public static ImageFrame[] readGIF(ImageReader reader) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>(2);

        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }
        }

        BufferedImage master = null;
        Graphics2D masterGraphics = null;

        for (int frameIndex = 0;; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = Integer.parseInt(gce.getAttribute("delayTime"));
            String disposal = gce.getAttribute("disposalMethod");

            int x = 0;
            int y = 0;

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else {
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.parseInt(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.parseInt(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }
            }
            masterGraphics.drawImage(image, x, y, null);

            BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
            frames.add(new ImageFrame(copy, delay, disposal, Math.max(height, width)));

            if (disposal.equals("restoreToPrevious")) {
                BufferedImage from = null;
                for (int i = frameIndex - 1; i >= 0; i--) {
                    if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                        from = frames.get(i).getImage();
                        break;
                    }
                }

                master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else if (disposal.equals("restoreToBackgroundColor")) {
                masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
            }
        }
        reader.dispose();

        return frames.toArray(new ImageFrame[0]);
    }

    public static class ImageFrame {
        private final int delay;
        private final BufferedImage image;
        private final String disposal;
        private final int height;

        public ImageFrame(BufferedImage image, int delay, String disposal, int height) {
            this.image = image;
            this.delay = delay;
            this.disposal = disposal;
            this.height = height;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelay() {
            return delay;
        }

        public String getDisposal() {
            return disposal;
        }

        public int getHeight() {
            return height;
        }
    }
}
