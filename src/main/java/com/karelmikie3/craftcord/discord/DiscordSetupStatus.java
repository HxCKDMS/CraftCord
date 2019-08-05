package com.karelmikie3.craftcord.discord;

public enum DiscordSetupStatus {
    STARTING,
    DONE(true),
    STOPPING,
    STOPPED,
    INVALID_BOT_TOKEN,
    INVALID_WEBHOOK_URL;

    private final boolean usable;

    DiscordSetupStatus() {
        usable = false;
    }

    DiscordSetupStatus(final boolean usable) {
        this.usable = usable;
    }

    public boolean isUsable() {
        return usable;
    }
}
