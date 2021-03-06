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

package com.karelmikie3.craftcord.proxy;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.discord.ClientDiscordHandler;
import com.karelmikie3.craftcord.resources.EmoteResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;

public class ClientProxy implements IProxy {
    @Override
    public void constructor(CraftCord mod) {
        final EmoteResourcePack resourcePack = new EmoteResourcePack();
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(resourcePack);
    }

    @Override
    public void setup(CraftCord mod) {

    }

    @Override
    public Object getClientDiscordHandler() {
        return new ClientDiscordHandler();
    }
}
