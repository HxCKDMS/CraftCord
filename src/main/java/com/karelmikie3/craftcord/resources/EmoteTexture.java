package com.karelmikie3.craftcord.resources;

import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;

public class EmoteTexture extends Texture implements ITickableTextureObject {
    protected final ResourceLocation textureLocation;
    private int frameCounter = 0;
    private int interFrameCounter = 0;
    private boolean isAnimated;
    private int frameAmount;
    private int height;
    private int[] delays;
    private NativeImage image;

    private final int SUB_FRAME_INCREMENT = 6;

    public EmoteTexture(ResourceLocation textureResourceLocation) {
        this.textureLocation = textureResourceLocation;
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        try (EmoteTextureData simpletexture$texturedata = this.func_215246_b(manager)) {
            simpletexture$texturedata.func_217801_c();

            GifSerializer.GifMetadataSection gifMetadataSection = simpletexture$texturedata.getGifMetadataSection();

            if (gifMetadataSection != null) {
                this.frameAmount = gifMetadataSection.getFrameAmount();
                this.isAnimated = gifMetadataSection.isAnimated();
                this.height = gifMetadataSection.getHeight();
                this.delays = gifMetadataSection.getDelays();
            }

            this.image = simpletexture$texturedata.func_217800_b();
            TextureUtil.prepareImage(this.getGlTextureId(), 0, image.getWidth(), this.isAnimated ? this.height : image.getHeight());
        }
    }

    protected EmoteTextureData func_215246_b(IResourceManager resourceManager) {
        return EmoteTextureData.func_217799_a(resourceManager, this.textureLocation);
    }

    @Override
    public void tick() {
        this.bindTexture();

        if (isAnimated) {
            image.uploadTextureSub(0, 0, 0, 0, frameCounter * height, image.getWidth(), height, true);

            if ((interFrameCounter += SUB_FRAME_INCREMENT) >= delays[frameCounter]) {
                interFrameCounter = 0;
                if (++frameCounter >= frameAmount) {
                    frameCounter = 0;
                }
            }
        } else {
            image.uploadTextureSub(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false);
        }
    }

    public static class EmoteTextureData extends SimpleTexture.TextureData {
        private final GifSerializer.GifMetadataSection gifMetadataSection;

        public EmoteTextureData(IOException p_i50473_1_) {
            super(p_i50473_1_);
            this.gifMetadataSection = null;
        }

        public EmoteTextureData(@Nullable TextureMetadataSection p_i50474_1_, NativeImage p_i50474_2_, @Nullable GifSerializer.GifMetadataSection gifMetadataSection) {
            super(p_i50474_1_, p_i50474_2_);
            this.gifMetadataSection = gifMetadataSection;
        }

        @Nullable
        public GifSerializer.GifMetadataSection getGifMetadataSection() {
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
