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

import com.karelmikie3.craftcord.api.Globals;
import com.karelmikie3.craftcord.api.presence.IMinecraftPresence;
import com.karelmikie3.craftcord.api.status.IMinecraftStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger();
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
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                LOGGER.warn("Using server config for token. This may be a security problem. Is this intended?");
            }

            return token;
        } else {
            return COMMON.DEFAULT_DISCORD_BOT_TOKEN.get();
        }
    }

    public static String getWebhookURL() {
        String webhookURL = SERVER.DISCORD_WEBHOOK_URL.get();

        if (webhookURL != null && !webhookURL.isEmpty()) {
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                LOGGER.warn("Using server config for webhook URL. This may be a security problem. Is this intended?");
            }

            return webhookURL;
        } else {
            return COMMON.DEFAULT_DISCORD_WEBHOOK_URL.get();
        }
    }

    public static boolean sameChannel() {
        return SERVER.SAME_CHANNEL.get();
    }

    public static boolean broadcastDiscordChat() {
        return SERVER.BROADCAST_DISCORD_CHAT.get();
    }

    public static boolean broadcastMinecraftChat() {
        return SERVER.BROADCAST_MC_CHAT.get();
    }

    public static boolean broadcastDeath() {
        return SERVER.BROADCAST_DEATH.get();
    }

    public static boolean broadcastAdvancement() {
        return SERVER.BROADCAST_ADVANCEMENT.get();
    }

    public static boolean broadcastServerStartStop() {
        return SERVER.BROADCAST_SERVER_START_STOP.get();
    }

    public static boolean broadcastPlayerJoinLeave() {
        return SERVER.BROADCAST_PLAYER_JOIN_LEAVE.get();
    }

    public static boolean broadcastCrash() {
        return SERVER.BROADCAST_CRASH.get();
    }

    public static boolean displayPresence() {
        return SERVER.DISPLAY_PRESENCE.get();
    }

    public static boolean displayStatus() {
        return SERVER.DISPLAY_STATUS.get();
    }

    public static long getStatusChannelID() {
        return SERVER.STATUS_CHANNEL_ID.get();
    }

    public static void setStatusChannelID(long id) {
        SERVER.STATUS_CHANNEL_ID.set(id);
        SERVER.STATUS_CHANNEL_ID.save();
    }

    public static long getStatusMessageID() {
        return SERVER.STATUS_MESSAGE_ID.get();
    }

    public static void setStatusMessageID(long id) {
        SERVER.STATUS_MESSAGE_ID.set(id);
        SERVER.STATUS_MESSAGE_ID.save();
    }

    public static int getPresenceStatusUpdateRate() {
        return SERVER.PRESENCE_STATUS_UPDATE_RATE.get();
    }

    public static List<IMinecraftPresence> getPresenceList() {
        return SERVER.PRESENCE_LIST.get().stream()
                .map(Globals.PRESENCE_REGISTRY::getPresence)
                .collect(Collectors.toList());
    }

    public static List<IMinecraftStatus> getStatusList() {
        return SERVER.STATUS_LIST.get().stream()
                .map(Globals.STATUS_REGISTRY::getStatus)
                .collect(Collectors.toList());
    }

    public static String getCommandCharacter() {
        return SERVER.DISCORD_COMMAND_CHARACTER.get();
    }

    public static boolean getGamemodeDisplay() {
        return SERVER.BROADCAST_GAMEMODE_IN_NAME.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteRenderingEnabled() {
        return CLIENT.ENABLE_EMOTE_RENDERING.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteSuggestionsEnabled() {
        return CLIENT.ENABLE_EMOTE_SUGGESTIONS.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean useCachedProvider() {
        return CLIENT.USE_CACHED_EMOTE_PROVIDER.get();
    }
}
