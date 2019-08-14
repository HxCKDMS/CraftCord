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

package com.karelmikie3.craftcord.api.presence;

import com.karelmikie3.craftcord.discord.IMinecraftPresence;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public class PresenceRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, IMinecraftPresence> presences = new HashMap<>();

    /**
     *
     * @param presenceEnumClass class of an enum that {@code implements} {@link IMinecraftPresence}.
     * @throws NullPointerException if {@code presenceEnumClass} is {@code null}.
     */
    public <T extends Enum & IMinecraftPresence> void registerPresence(Class<T> presenceEnumClass) {
        Objects.requireNonNull(presenceEnumClass);
        T[] values = presenceEnumClass.getEnumConstants();

        if (values != null) {
            LOGGER.debug("Registering presence enum.");
            for (T value : values) {
                registerPresence(value.name(), value);
            }
        }
    }

    /**
     *
     * @param name internal and config name of this presence.
     * @param presence the presence object to be associated with this name.
     * @throws NullPointerException if either {@code name} or {@code presence} are {@code null}.
     * @throws IllegalArgumentException if the name is blank.
     */
    public void registerPresence(String name, IMinecraftPresence presence) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Presence name may not be blank.");

        Objects.requireNonNull(presence);

        LOGGER.debug("Registering presence with name {}.", name);

        presences.put(name, presence);
    }

    /**
     * @return a {@link Collection<String>} of all registered presence names.
     */
    public Collection<String> getAllPresenceNames() {
        return presences.keySet();
    }

    /**
     * @param name the name of the {@linkplain IMinecraftPresence} to check.
     * @return {@code true} if the {@linkplain IMinecraftPresence} exists, {@code false} otherwise.
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the name is blank.
     */
    public boolean doesPresenceExist(String name) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Presence name may not be blank.");

        return presences.containsKey(name);
    }

    /**
     * @param name the name of the requested {@linkplain IMinecraftPresence}.
     * @return the {@link IMinecraftPresence} associated with this name.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if the name is blank or if there is no {@linkplain IMinecraftPresence} associated with this name.
     */
    public IMinecraftPresence getPresence(String name) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Presence name may not be blank.");

        if (!presences.containsKey(name))
            throw new IllegalArgumentException("Presence '" + name + "' does not exist.");

        return presences.get(name);
    }
}
