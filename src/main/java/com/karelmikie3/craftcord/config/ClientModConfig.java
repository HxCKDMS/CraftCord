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

package com.karelmikie3.craftcord.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

class ClientModConfig {
    private static final ForgeConfigSpec clientSpec;
    static final ClientModConfig CONFIG;

    static {
        final Pair<ClientModConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientModConfig::new);
        clientSpec = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    final ForgeConfigSpec.BooleanValue ENABLE_EMOTE_RENDERING;
    final ForgeConfigSpec.BooleanValue ENABLE_EMOTE_SUGGESTIONS;
    final ForgeConfigSpec.BooleanValue USE_CACHED_EMOTE_PROVIDER;

    private ClientModConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Client settings")
                .push("client");

        this.ENABLE_EMOTE_RENDERING = builder
                .comment("Renders emotes as seen in Discord.")
                .translation("craftcord.configgui.enableEmoteRendering")
                .define("Enable emote rendering", true);

        this.ENABLE_EMOTE_SUGGESTIONS = builder
                .comment("Enables emote suggestions in chat if you start typing ':'.")
                .translation("craftcord.configgui.enableEmoteSuggestions")
                .define("Enable emote suggestions", true);

        this.USE_CACHED_EMOTE_PROVIDER = builder
                .comment("Cache the rendered emotes.", "If disabled emotes will be stored in memory and will be downloaded every session")
                .translation("craftcord.configgui.useCachedProvider")
                .define("Use cached emote provider", true);

        builder.pop();

    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }
}
