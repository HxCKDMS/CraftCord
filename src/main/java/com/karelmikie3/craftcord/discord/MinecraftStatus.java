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
import com.karelmikie3.craftcord.util.TimeHelper;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.function.Function;

public enum MinecraftStatus implements IMinecraftStatus {
    TEST("Test title", "Test message"),
    TPS(server -> "TPS", TimeHelper::getMeanTPS);

    private final Function<MinecraftServer, String> title;
    private final Function<MinecraftServer, String> message;

    MinecraftStatus(Function<MinecraftServer, String> title, Function<MinecraftServer, String> message) {
        this.title = title;
        this.message = message;
    }

    MinecraftStatus(@Nullable String title, @Nullable String message) {
        this(server -> title, server -> message);
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
