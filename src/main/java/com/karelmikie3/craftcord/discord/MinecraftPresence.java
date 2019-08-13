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

import net.minecraft.server.MinecraftServer;

import java.util.function.Function;
import java.util.function.Supplier;

public enum MinecraftPresence implements IMinecraftPresence {

    TEST("test"),
    AMOUNT_PLAYING(server -> server.getCurrentPlayerCount() + " player(s) are playing on the server"),
    AMOUNT_PLAYING_OF_TOTAL(server -> server.getCurrentPlayerCount() + "/" + server.getMaxPlayers() + " playing.");


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
