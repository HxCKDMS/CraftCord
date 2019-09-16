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

import com.google.common.collect.Sets;
import com.karelmikie3.craftcord.CraftCord;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import javax.annotation.Nullable;
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
        String emoteIDString = resourcePath.replace("assets/craftcordemotes/textures/emotedata/", "").replaceAll("\\.[^.]*$", "");
        long emoteID = Long.parseLong(emoteIDString);

        if (resourcePath.endsWith(".mcmeta"))
            return CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.getInput(emoteID, true);


        return CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.getInput(emoteID, false);
    }

    @Override
    protected boolean resourceExists(String resourcePath) {
        if (resourcePath.endsWith(".json"))
            return false;

        String emoteIDString = resourcePath.replace("assets/craftcordemotes/textures/emotedata/", "").replaceAll("\\.[^.]*$", "");
        long emoteID = Long.parseLong(emoteIDString);

        return CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.exists(emoteID);
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
