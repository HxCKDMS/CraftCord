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

        builder.pop();
    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
    }
}
