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

package com.karelmikie3.craftcord.discord;

import com.karelmikie3.craftcord.api.emotes.IEmoteProvider;
import com.karelmikie3.craftcord.config.Config;
import com.karelmikie3.craftcord.resources.CachedEmoteProvider;
import com.karelmikie3.craftcord.resources.MemoryEmoteProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientDiscordHandler {
    public final IEmoteProvider emoteProvider = Config.useCachedProvider() ?
            new CachedEmoteProvider() :
            new MemoryEmoteProvider();
}
