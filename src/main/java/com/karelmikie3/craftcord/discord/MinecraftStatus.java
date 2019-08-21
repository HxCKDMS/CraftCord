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

package com.karelmikie3.craftcord.discord;

import com.karelmikie3.craftcord.api.status.IMinecraftStatus;
import com.karelmikie3.craftcord.util.DiscordHelper;
import com.karelmikie3.craftcord.util.StatusPresenceHelper;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum MinecraftStatus implements IMinecraftStatus {
    TEST("Test title", "Test message"),
    TPS_AND_TICK_TIME("TPS and Tick time", StatusPresenceHelper::getAllMeanTPSAndTickTime),
    PLAYERS(server -> String.format("Player list (%d/%d)", server.getCurrentPlayerCount(), server.getMaxPlayers()), StatusPresenceHelper::getPlayerList),
    SCOREBOARD(server -> {
        ScoreObjective objective = server.getScoreboard().getObjectiveInDisplaySlot(1);
        return String.format("Scoreboard: %s", objective != null ? DiscordHelper.stripFormattingForDiscord(objective.getDisplayName().getFormattedText()) : "none");
    }, StatusPresenceHelper::getScoreboard),
    WEATHER("Weather", server -> server.getWorld(DimensionType.OVERWORLD).isThundering() ? "Thunderstorm" : server.getWorld(DimensionType.OVERWORLD).isRaining() ? "Rainstorm" : "Clear"),
    SEED("Seed", server -> Long.toString(server.getWorld(DimensionType.OVERWORLD).getWorldInfo().getSeed())),
    DIFFICULTY("Difficulty", server -> server.isHardcore() ? "HARDCORE" : server.getDifficulty().name()),
    TIME_AND_DAY("Day and Time", server -> String.format("Day: %d\nTime: %s", server.getWorld(DimensionType.OVERWORLD).getDayTime() / 24000L % 2147483647L, StatusPresenceHelper.getTimeOfDay(server.getWorld(DimensionType.OVERWORLD).getDayTime()))),
    MOB_AMOUNT_AND_CAP("Mob amount / Mob cap", StatusPresenceHelper::getMobsAndCap);


    private final Function<MinecraftServer, String> title;
    private final Function<MinecraftServer, String> message;

    MinecraftStatus(Function<MinecraftServer, String> title, Function<MinecraftServer, String> message) {
        this.title = title;
        this.message = message;
    }

    MinecraftStatus(@Nullable String title, Function<MinecraftServer, String> message) {
        this(server -> title, message);
    }


    MinecraftStatus(@Nullable String title, @Nullable String message) {
        this(title, server -> message);
    }

    @Override
    public Function<MinecraftServer, String> getTitle() {
        return title;
    }

    @Override
    public Function<MinecraftServer, String> getMessage() {
        return message;
    }
}
