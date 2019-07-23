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
    private GifSerializer.GifMetadataSection gifMetadataSection;
    private int counter = 0;
    private NativeImage image;

    public EmoteTexture(ResourceLocation textureResourceLocation) {
        this.textureLocation = textureResourceLocation;
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        try (EmoteTextureData simpletexture$texturedata = this.func_215246_b(manager)) {
            simpletexture$texturedata.func_217801_c();

            this.gifMetadataSection = simpletexture$texturedata.getGifMetadataSection();
            this.image = simpletexture$texturedata.func_217800_b();
            TextureUtil.prepareImage(this.getGlTextureId(), 0, image.getWidth(), gifMetadataSection.getHeight() != -1 ? gifMetadataSection.getHeight() : image.getHeight());
        }
    }

    protected EmoteTextureData func_215246_b(IResourceManager resourceManager) {
        return EmoteTextureData.func_217799_a(resourceManager, this.textureLocation);
    }

    @Override
    public void tick() {
        this.bindTexture();

        if (gifMetadataSection.isAnimated()) {

            image.uploadTextureSub(0, 0, 0, 0, counter * gifMetadataSection.getHeight(), image.getWidth(), gifMetadataSection.getHeight(), false);

            if (++counter >= gifMetadataSection.getFrameAmount()) {
                counter = 0;
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

        public EmoteTextureData(@Nullable TextureMetadataSection p_i50474_1_, NativeImage p_i50474_2_, GifSerializer.GifMetadataSection gifMetadataSection) {
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