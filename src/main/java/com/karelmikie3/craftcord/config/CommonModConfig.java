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

class CommonModConfig {
    private static final ForgeConfigSpec commonSpec;
    static final CommonModConfig CONFIG;

    static {
        final Pair<CommonModConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonModConfig::new);
        commonSpec = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    final ForgeConfigSpec.ConfigValue<String> DEFAULT_DISCORD_BOT_TOKEN;
    final ForgeConfigSpec.ConfigValue<String> DEFAULT_DISCORD_WEBHOOK_URL;

    private CommonModConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Common settings")
                .push("common");

        this.DEFAULT_DISCORD_BOT_TOKEN = builder
                //TODO: expand as more features are added
                .comment("Default token for the Discord bot. Can be set per world.",
                        "Used for reading messages from the server.",
                        "For a dedicated server use this one over the server config as this one won't be synchronized to the player.")
                .translation("craftcord.configgui.defaultDiscordBotToken")
                .define("Default Discord bot token", "");

        this.DEFAULT_DISCORD_WEBHOOK_URL = builder
                .comment("Default discord webhook URL. Can be set per world.",
                        "Used to post messages to the server.",
                        "For a dedicated server use this one over the server config as this one won't be synchronized to the player.")
                .translation("craftcord.configgui.defaultDiscordWebhookURL")
                .define("Default Discord webhook URL", "");

        builder.pop();
    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec);
    }
}
