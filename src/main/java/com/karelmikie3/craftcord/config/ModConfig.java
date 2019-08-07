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
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModConfig {
    private static final CommonModConfig COMMON = CommonModConfig.CONFIG;
    private static final ClientModConfig CLIENT = ClientModConfig.CONFIG;
    private static final ServerModConfig SERVER = ServerModConfig.CONFIG;

    public static void initConfigs() {
        ClientModConfig.initConfig();
        CommonModConfig.initConfig();
        ServerModConfig.initConfig();
    }

    public static String getBotToken() {
        String token = SERVER.DISCORD_BOT_TOKEN.get();

        if (token != null && !token.isEmpty()) {
            return token;
        } else {
            return COMMON.DEFAULT_DISCORD_BOT_TOKEN.get();
        }
    }

    public static String getWebhookURL() {
        String webhookURL = SERVER.DISCORD_WEBHOOK_URL.get();

        if (webhookURL != null && !webhookURL.isEmpty()) {
            return webhookURL;
        } else {
            return COMMON.DEFAULT_DISCORD_WEBHOOK_URL.get();
        }
    }

    public static boolean sameChannel() {
        return SERVER.SAME_CHANNEL.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteRenderingEnabled() {
        return CLIENT.ENABLE_EMOTE_RENDERING.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteSuggestionsEnabled() {
        return CLIENT.ENABLE_EMOTE_SUGGESTIONS.get();
    }
}
