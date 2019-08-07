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
                .comment("Default token for the Discord bot. Can be set per world.", "Used for reading messages from the server.")
                .translation("craftcord.configgui.defaultDiscordBotToken")
                .define("Default Discord bot token", "");

        this.DEFAULT_DISCORD_WEBHOOK_URL = builder
                .comment("Default discord webhook URL. Can be set per world.", "Used to post messages to the server.")
                .translation("craftcord.configgui.defaultDiscordWebhookURL")
                .define("Default Discord webhook URL", "");

        builder.pop();
    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec);
    }
}
