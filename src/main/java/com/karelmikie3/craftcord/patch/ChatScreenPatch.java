package com.karelmikie3.craftcord.patch;

import com.karelmikie3.craftcord.config.ModConfig;
import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.ISuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ChatScreenPatch {

    @SuppressWarnings("unused")
    public static void addSuggestions(ChatScreen instance, String s) {
        if (!ModConfig.emoteSuggestionsEnabled())
            return;

        boolean buildingEmote = false;
        boolean longerThanZero = false;
        int currentColonIndex = 0;

        char[] charArray = s.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];

            if (c == ':' && !buildingEmote) {
                buildingEmote = true;
                currentColonIndex = i;
            } else if ((c >= 'a' && 'z' >= c || c >= 'A' && 'Z' >= c || c >= '0' && '9' >= c || c == '-') && buildingEmote) {
                longerThanZero = true;
            } else if (c == ':' && longerThanZero) {
                buildingEmote = false;
            }
        }

        if (buildingEmote) {
            instance.pendingSuggestions = ISuggestionProvider.suggest(ClientEmoteHelper.getUsableEmotes().stream().map(emote -> ":" + emote + ":"), new SuggestionsBuilder(s, currentColonIndex));

            instance.pendingSuggestions.thenRun(() -> {
                if (instance.pendingSuggestions.isDone()) {
                    instance.showSuggestions();
                }
            });
        }

    }
}
