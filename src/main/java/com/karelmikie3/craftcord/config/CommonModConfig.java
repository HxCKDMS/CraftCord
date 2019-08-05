package com.karelmikie3.craftcord.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class CommonModConfig {
    private static final ForgeConfigSpec commonSpec;
    public static final CommonModConfig CONFIG;

    static {
        final Pair<CommonModConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonModConfig::new);
        commonSpec = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public final ForgeConfigSpec.ConfigValue<String> DISCORD_BOT_TOKEN;
    public final ForgeConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_URL;

    private CommonModConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Common settings")
                .push("common");

        this.DISCORD_BOT_TOKEN = builder
                //TODO: expand as more features are added
                .comment("Token for the Discord bot.", "Used for reading messages from the server.")
                .translation("craftcord.configgui.discordBotToken")
                .define("Discord bot token", "");

        this.DISCORD_WEBHOOK_URL = builder
                .comment("Discord webhook URL", "Used to post messages to the server.")
                .translation("craftcord.configgui.discordWebhookURL")
                .define("Discord webhook URL", "");

        builder.pop();
    }

    public static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec);
    }
}
