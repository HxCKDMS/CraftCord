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

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteRenderingEnabled() {
        return CLIENT.ENABLE_EMOTE_RENDERING.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean emoteSuggestionsEnabled() {
        return CLIENT.ENABLE_EMOTE_SUGGESTIONS.get();
    }
}
