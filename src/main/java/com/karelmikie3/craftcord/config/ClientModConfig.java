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

        builder.pop();

    }

    static void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
    }
}
