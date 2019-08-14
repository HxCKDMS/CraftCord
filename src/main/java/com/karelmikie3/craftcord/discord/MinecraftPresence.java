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

import com.karelmikie3.craftcord.util.TimeHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Function;
import java.util.function.Supplier;

public enum MinecraftPresence implements IMinecraftPresence {
    AMOUNT_PLAYING(server -> server.getCurrentPlayerCount() + String.format(" player%s playing", server.getCurrentPlayerCount() == 1 ? "" : "s")),
    AMOUNT_PLAYING_OF_TOTAL(server -> server.getCurrentPlayerCount() + "/" + server.getMaxPlayers() + " players"),
    AMOUNT_PLAYING_AND_TOTAL(server -> server.getCurrentPlayerCount() + String.format(" player%s playing", server.getCurrentPlayerCount() == 1 ? "" : "s") + String.format(" (%d/%d)", server.getCurrentPlayerCount(), server.getMaxPlayers())),
    DAY((server) -> "Day: " + server.getWorld(DimensionType.OVERWORLD).getDayTime() / 24000L % 2147483647L),
    /**
     * shows time of the day in the overworld.
     */
    TIME_OF_DAY((server) -> "Time: " + TimeHelper.getTimeOfDay(server.getWorld(DimensionType.OVERWORLD).getDayTime())),
    /**
     * shows server tps.
     */
    TPS((server) -> "Server TPS: " + TimeHelper.getMeanTPS(server)),
    /**
     * shows weather in the overworld.
     */
    WEATHER((server) -> "Weather: " + (server.getWorld(DimensionType.OVERWORLD).isThundering() ? "Thunderstorm" : server.getWorld(DimensionType.OVERWORLD).isRaining() ? "Rainstorm" : "Clear")),
    DIFFICULTY((server) -> "Difficulty: " + (server.isHardcore() ? "HARDCORE" : server.getDifficulty().name())),
    HARDCORE((server) -> server.isHardcore() ? "Playing on hardcore" : "Not playing on hardcore"),
    SEED((server) -> "Seed: " + server.getWorld(DimensionType.OVERWORLD).getWorldInfo().getSeed());


    private final Function<MinecraftServer, String> message;

    MinecraftPresence(final Function<MinecraftServer, String> message) {
        this.message = message;
    }

    MinecraftPresence(final Supplier<String> message) {
        this.message = (server) -> message.get();
    }

    MinecraftPresence(final String message) {
        this((server) -> message);
    }

    @Override
    public Function<MinecraftServer, String> getMessage() {
        return message;
    }
}
