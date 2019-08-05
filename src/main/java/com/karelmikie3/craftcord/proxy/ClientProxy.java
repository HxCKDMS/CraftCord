package com.karelmikie3.craftcord.proxy;

import com.karelmikie3.craftcord.resources.EmoteResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;

public class ClientProxy implements IProxy {
    @Override
    public void constructor() {
        final EmoteResourcePack resourcePack = new EmoteResourcePack();
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(resourcePack);
    }

    @Override
    public void setup() {

    }
}
