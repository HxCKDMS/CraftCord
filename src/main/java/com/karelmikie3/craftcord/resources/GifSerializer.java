package com.karelmikie3.craftcord.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;

import java.util.Arrays;
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
        final JsonArray jsonArray = JSONUtils.getJsonArray(json, "delays");
        final int[] delays = new int[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            delays[i] = jsonArray.get(i).getAsInt();
        }

        return new GifMetadataSection(frameAmount, height, animated, delays);
    }

    public static class GifMetadataSection {
        private final int frameAmount;
        private final int height;
        private final boolean animated;
        private final int[] delays;

        public GifMetadataSection(int frameAmount, int height, boolean animated, int[] delays) {
            this.frameAmount = frameAmount;
            this.height = height;
            this.animated = animated;
            this.delays = delays;
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

        public int[] getDelays() {
            return delays;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GifMetadataSection)) return false;
            GifMetadataSection that = (GifMetadataSection) o;
            return getFrameAmount() == that.getFrameAmount() &&
                    getHeight() == that.getHeight() &&
                    isAnimated() == that.isAnimated() &&
                    Arrays.equals(getDelays(), that.getDelays());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(getFrameAmount(), getHeight(), isAnimated());
            result = 31 * result + Arrays.hashCode(getDelays());
            return result;
        }

        @Override
        public String toString() {
            return "GifMetadataSection{" +
                    "frameAmount=" + frameAmount +
                    ", height=" + height +
                    ", animated=" + animated +
                    ", delays=" + Arrays.toString(delays) +
                    '}';
        }
    }
}
