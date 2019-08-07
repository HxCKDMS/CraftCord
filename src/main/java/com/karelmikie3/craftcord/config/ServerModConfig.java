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

class ServerModConfig {
    private static final ForgeConfigSpec serverSpec;
    static final ServerModConfig CONFIG;

    static {
        final Pair<ServerModConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerModConfig::new);
        serverSpec = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    final ForgeConfigSpec.ConfigValue<String> DISCORD_BOT_TOKEN;
    final ForgeConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_URL;
    final ForgeConfigSpec.BooleanValue SAME_CHANNEL;

    private ServerModConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Server settings")
                .push("server");

        this.DISCORD_BOT_TOKEN = builder
                //TODO: expand as more features are added
                .comment("Token for the Discord bot.", "Used for reading messages from the server.")
                .translation("craftcord.configgui.discordBotToken")
                .define("Discord bot token", "");

        this.DISCORD_WEBHOOK_URL = builder
                .comment("Discord webhook URL.", "Used to post messages to the server.")
                .translation("craftcord.configgui.discordWebhookURL")
                .define("Discord webhook URL", "");

        this.SAME_CHANNEL = builder
                .comment("Only Display messages received on the same channel as the webhook.", "Disable to display all messages on the same Server.")
                .translation("craftcord.configgui.sameChannel")
                .define("Same channel", true);

        builder.pop();
    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
    }
}
