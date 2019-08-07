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

package com.karelmikie3.craftcord.event;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.discord.DiscordSetupStatus;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntityEvents {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            if (player.getServer().getPlayerList().canSendCommands(event.getPlayer().getGameProfile())) {
                if (CraftCord.getInstance().DISCORD_HANDLER.getBotStatus() == DiscordSetupStatus.INVALID_BOT_TOKEN) {
                    ITextComponent component = new TranslationTextComponent("craftcord.bot.invalidToken")
                            .applyTextStyle(TextFormatting.DARK_RED);

                    player.sendMessage(component);
                } else if (!CraftCord.getInstance().DISCORD_HANDLER.getBotStatus().isUsable()) {
                    ITextComponent component = new TranslationTextComponent("craftcord.bot.notRunning")
                            .applyTextStyle(TextFormatting.DARK_RED);

                    player.sendMessage(component);
                }

                if (CraftCord.getInstance().DISCORD_HANDLER.getWebhookStatus() == DiscordSetupStatus.INVALID_WEBHOOK_URL) {
                    ITextComponent component = new TranslationTextComponent("craftcord.webhook.invalidURL")
                            .applyTextStyle(TextFormatting.DARK_RED);

                    player.sendMessage(component);
                } else if (!CraftCord.getInstance().DISCORD_HANDLER.getBotStatus().isUsable()) {
                    ITextComponent component = new TranslationTextComponent("craftcord.webhook.notRunning")
                            .applyTextStyle(TextFormatting.DARK_RED);

                    player.sendMessage(component);
                }
            }

            CraftCord.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new SendEmotesMessageS2C(true));
        }
    }
}
