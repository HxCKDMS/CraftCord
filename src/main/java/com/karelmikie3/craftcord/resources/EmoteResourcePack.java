package com.karelmikie3.craftcord.resources;

import com.google.common.collect.Sets;
import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class EmoteResourcePack extends ResourcePack implements ISelectiveResourceReloadListener {
    private static final Minecraft mc = Minecraft.getInstance();

    public EmoteResourcePack() {
        super(new File("dummy"));
    }

    @Override
    protected InputStream getInputStream(String resourcePath) {
        String usefulResourcePath = resourcePath.replace("assets/craftcordemotes/textures/emotedata/", "").replaceAll("\\.[^.]*$", "");

        if (resourcePath.endsWith(".mcmeta"))
            return new ByteArrayInputStream(ClientEmoteHelper.getEmoteMetadata(usefulResourcePath));


        return new ByteArrayInputStream(ClientEmoteHelper.getEmoteData(usefulResourcePath));
    }

    @Override
    protected boolean resourceExists(String resourcePath) {
        if (resourcePath.endsWith(".lang"))
            return false;

        return ClientEmoteHelper.hasEmoteData(resourcePath.replace("assets/craftcordemotes/textures/emotedata/", "").replaceAll("\\.[^.]*$", ""));
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String pathIn, int maxDepth, Predicate<String> filter) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getResourceNamespaces(ResourcePackType type) {
        return Sets.newHashSet("craftcordemotes");
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
    public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) {
        return null;
    }
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        mc.getResourceManager().addResourcePack(this);
    }
}
