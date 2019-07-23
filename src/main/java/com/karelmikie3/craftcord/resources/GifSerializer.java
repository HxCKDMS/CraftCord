package com.karelmikie3.craftcord.resources;

import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;

import java.util.Objects;

public class GifSerializer implements IMetadataSectionSerializer<GifSerializer.GifMetadataSection> {
    public static final GifSerializer SERIALIZER = new GifSerializer();

    @Override
    public String getSectionName() {
        return "test";
    }

    @Override
    public GifMetadataSection deserialize(JsonObject json) {
        final int frameAmount = JSONUtils.getInt(json, "frameAmount");
        final int height = JSONUtils.getInt(json, "height");
        final boolean animated = JSONUtils.getBoolean(json, "animated");

        return new GifMetadataSection(frameAmount, height, animated);
    }

    public static class GifMetadataSection {
        private final int frameAmount;
        private final int height;
        private final boolean animated;

        public GifMetadataSection(int frameAmount, int height, boolean animated) {
            this.frameAmount = frameAmount;
            this.height = height;
            this.animated = animated;
        }

        public int getFrameAmount() {
            return frameAmount;
        }

        public int getHeight() {
            return height;
        }

        public boolean isAnimated() {
            return animated;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GifMetadataSection)) return false;
            GifMetadataSection that = (GifMetadataSection) o;
            return getFrameAmount() == that.getFrameAmount() &&
                    getHeight() == that.getHeight() &&
                    isAnimated() == that.isAnimated();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFrameAmount(), getHeight(), isAnimated());
        }

        @Override
        public String toString() {
            return "GifMetadataSection{" +
                    "frameAmount=" + frameAmount +
                    ", height=" + height +
                    ", animated=" + animated +
                    '}';
        }
    }
}
