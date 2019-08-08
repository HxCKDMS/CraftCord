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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
    final ForgeConfigSpec.BooleanValue BROADCAST_DISCORD_CHAT;
    final ForgeConfigSpec.BooleanValue BROADCAST_MC_CHAT;
    final ForgeConfigSpec.BooleanValue SAME_CHANNEL;
    final ForgeConfigSpec.BooleanValue BROADCAST_DEATH;
    final ForgeConfigSpec.BooleanValue BROADCAST_ADVANCEMENT;
    final ForgeConfigSpec.BooleanValue BROADCAST_SERVER_START_STOP;
    final ForgeConfigSpec.BooleanValue BROADCAST_PLAYER_JOIN_LEAVE;
    final ForgeConfigSpec.BooleanValue BROADCAST_CRASH;

    private ServerModConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Server settings")
                .push("server");

        this.DISCORD_BOT_TOKEN = builder
                //TODO: expand as more features are added
                .comment("WARNING: on a dedicated server use the common config over this one.",
                        "This config will be synchronized to the player and your token may be compromised.",
                        "Token for the Discord bot.",
                        "Used for reading messages from the server.")
                .translation("craftcord.configgui.discordBotToken")
                .define("Discord bot token", "");

        this.DISCORD_WEBHOOK_URL = builder
                .comment("WARNING: on a dedicated server use the common config over this one",
                        "This config will be synchronized to the player and your webhook URL may be compromised.",
                        "Discord webhook URL.",
                        "Used to post messages to the server.")
                .translation("craftcord.configgui.discordWebhookURL")
                .define("Discord webhook URL", "");

        this.BROADCAST_DISCORD_CHAT = builder
                .comment("Send Discord messages to Minecraft chat.")
                .translation("craftcord.configgui.DC2MC")
                .define("Broadcast Discord chat", true);

        this.BROADCAST_MC_CHAT = builder
                .comment("Send Minecraft chat messages to Discord")
                .translation("craftcord.configgui.MC2DC")
                .define("Broadcast Minecraft chat", true);

        this.SAME_CHANNEL = builder
                .comment("Only Display messages received on the same channel as the webhook.", "Disable to display all messages on the same Server.")
                .translation("craftcord.configgui.sameChannel")
                .define("Same channel", true);

        this.BROADCAST_DEATH = builder
                .comment("Broadcast player death to Discord.")
                .translation("craftcord.configgui.broadcastDeath")
                .define("Broadcast death", true);

        this.BROADCAST_ADVANCEMENT = builder
                .comment("Broadcast advancements made to Discord.")
                .translation("craftcord.configgui.broadcastAdvancement")
                .define("Broadcast advancement", true);

        this.BROADCAST_SERVER_START_STOP = builder
                .comment("Broadcast to Discord when the server or single player world is started or stopped.")
                .translation("craftcord.configgui.broadcastServerStartStop")
                .define("Broadcast server start/stop", FMLEnvironment.dist == Dist.DEDICATED_SERVER);

        this.BROADCAST_PLAYER_JOIN_LEAVE = builder
                .comment("Broadcast players joining or leaving to Discord.")
                .translation("craftcord.configgui.broadcastJoinLeave")
                .define("Broadcast player join/leave", FMLEnvironment.dist == Dist.DEDICATED_SERVER);

        this.BROADCAST_CRASH = builder
                .comment("Broadcast to Discord a crash occurs.")
                .translation("craftcord.configgui.broadcastCrash")
                .define("Broadcast crash", FMLEnvironment.dist == Dist.DEDICATED_SERVER);

        builder.pop();
    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
    }
}
