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

import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;

public class EmoteTexture extends Texture implements ITickable {
    private final ResourceLocation textureLocation;
    private int frameCounter = 0;
    private int interFrameCounter = 0;
    private boolean isAnimated;
    private int frameAmount;
    private int height;
    private int[] delays;
    private NativeImage image;

    private static final int SUB_FRAME_INCREMENT = 6;

    public EmoteTexture(ResourceLocation textureResourceLocation) {
        this.textureLocation = textureResourceLocation;
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        try (EmoteTextureData simpletexture$texturedata = this.func_215246_b(manager)) {
            simpletexture$texturedata.checkException();

            GifSerializer.GifMetadataSection gifMetadataSection = simpletexture$texturedata.getGifMetadataSection();

            if (gifMetadataSection != null) {
                this.frameAmount = gifMetadataSection.getFrameAmount();
                this.isAnimated = gifMetadataSection.isAnimated();
                this.height = gifMetadataSection.getHeight();
                this.delays = gifMetadataSection.getDelays();
            }

            this.image = simpletexture$texturedata.getNativeImage();
            TextureUtil.prepareImage(this.getGlTextureId(), 0, image.getWidth(), this.isAnimated ? this.height : image.getHeight());
        }
    }

    private EmoteTextureData func_215246_b(IResourceManager resourceManager) {
        return EmoteTextureData.func_217799_a(resourceManager, this.textureLocation);
    }

    @Override
    //TODO: move frameCounter to ClientTickEvent
    public void tick() {
        this.bindTexture();

        if (isAnimated) {
            image.uploadTextureSub(0, 0, 0, 0, frameCounter * height, image.getWidth(), height, false, false);

            if ((interFrameCounter += SUB_FRAME_INCREMENT) >= delays[frameCounter]) {
                interFrameCounter = 0;
                if (++frameCounter >= frameAmount) {
                    frameCounter = 0;
                }
            }
        } else {
            image.uploadTextureSub(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, false);
        }
    }

    public static class EmoteTextureData extends SimpleTexture.TextureData {
        private final GifSerializer.GifMetadataSection gifMetadataSection;

        EmoteTextureData(IOException p_i50473_1_) {
            super(p_i50473_1_);
            this.gifMetadataSection = null;
        }

        EmoteTextureData(@Nullable TextureMetadataSection p_i50474_1_, NativeImage p_i50474_2_, @Nullable GifSerializer.GifMetadataSection gifMetadataSection) {
            super(p_i50474_1_, p_i50474_2_);
            this.gifMetadataSection = gifMetadataSection;
        }

        @Nullable
        GifSerializer.GifMetadataSection getGifMetadataSection() {
            return gifMetadataSection;
        }

        public static EmoteTextureData func_217799_a(IResourceManager p_217799_0_, ResourceLocation p_217799_1_) {
            try (IResource iresource = p_217799_0_.getResource(p_217799_1_)) {
                NativeImage nativeimage = NativeImage.read(iresource.getInputStream());
                TextureMetadataSection texturemetadatasection = null;
                GifSerializer.GifMetadataSection gifMetadataSection = null;

                try {
                    texturemetadatasection = iresource.getMetadata(TextureMetadataSection.SERIALIZER);
                } catch (RuntimeException runtimeexception) {
                    runtimeexception.printStackTrace();
                }

                try {
                    gifMetadataSection = iresource.getMetadata(GifSerializer.SERIALIZER);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

                return new EmoteTextureData(texturemetadatasection, nativeimage, gifMetadataSection);
            } catch (IOException ioexception) {
                return new EmoteTextureData(ioexception);
            }
        }

        @Override
        public void close() {
            //super.close();
        }
    }
}
