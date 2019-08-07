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

package com.karelmikie3.craftcord.patch;

import com.karelmikie3.craftcord.config.ModConfig;
import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.command.ISuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class ChatScreenPatch {

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
