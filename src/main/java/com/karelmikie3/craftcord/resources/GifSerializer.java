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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;

import java.util.Arrays;
import java.util.Objects;

public class GifSerializer implements IMetadataSectionSerializer<GifSerializer.GifMetadataSection> {
    static final GifSerializer SERIALIZER = new GifSerializer();

    @Override
    public String getSectionName() {
        return "emote";
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

        GifMetadataSection(int frameAmount, int height, boolean animated, int[] delays) {
            this.frameAmount = frameAmount;
            this.height = height;
            this.animated = animated;
            this.delays = delays;
        }

        int getFrameAmount() {
            return frameAmount;
        }

        int getHeight() {
            return height;
        }

        boolean isAnimated() {
            return animated;
        }

        int[] getDelays() {
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
