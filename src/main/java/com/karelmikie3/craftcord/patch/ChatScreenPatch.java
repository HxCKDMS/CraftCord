package com.karelmikie3.craftcord.patch;

import com.karelmikie3.craftcord.util.EmoteHelper;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.ISuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ChatScreenPatch {
    public static void addSuggestions(ChatScreen instance, String s) {

        boolean buildingEmote = false;
        boolean longerThanOne = false;
        int currentColonIndex = 0;

        char[] charArray = s.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];

            if (c == ':' && !buildingEmote) {
                buildingEmote = true;
                currentColonIndex = i;
            } else if ((c >= 'a' && 'z' >= c || c >= 'A' && 'Z' >= c || c >= '0' && '9' >= c || c == '-') && buildingEmote) {
                longerThanOne = true;
            } else if (c == ':' && longerThanOne) {
                buildingEmote = false;
            }
        }

        if (buildingEmote) {
            instance.pendingSuggestions = ISuggestionProvider.suggest(EmoteHelper.getEmotes().stream().map(emote -> ":" + emote + ":"), new SuggestionsBuilder(s, currentColonIndex));

            instance.pendingSuggestions.thenRun(() -> {
                if (instance.pendingSuggestions.isDone()) {
                    instance.showSuggestions();
                }
            });
        }

    }
}
