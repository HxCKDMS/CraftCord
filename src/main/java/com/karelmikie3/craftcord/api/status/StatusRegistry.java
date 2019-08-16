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

package com.karelmikie3.craftcord.api.status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class StatusRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, IMinecraftStatus> statuses = new HashMap<>();

    /**
     *
     * @param statusEnumClass class of an enum that {@code implements} {@link IMinecraftStatus}.
     * @throws NullPointerException if {@code statusEnumClass} is {@code null}.
     */
    public <T extends Enum & IMinecraftStatus> void registerStatus(Class<T> statusEnumClass) {
        Objects.requireNonNull(statusEnumClass);
        T[] values = statusEnumClass.getEnumConstants();

        if (values != null) {
            LOGGER.debug("Registering status enum.");
            for (T value : values) {
                registerStatus(value.name(), value);
            }
        }
    }

    /**
     *
     * @param name internal and config name of this status.
     * @param status the status object to be associated with this name.
     * @throws NullPointerException if either {@code name} or {@code status} are {@code null}.
     * @throws IllegalArgumentException if the name is blank.
     */
    public void registerStatus(String name, IMinecraftStatus status) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Status name may not be blank.");

        Objects.requireNonNull(status);

        LOGGER.debug("Registering status with name {}.", name);

        statuses.put(name, status);
    }

    /**
     * @return a {@link Collection<String>} of all registered status names.
     */
    public Collection<String> getAllStatusNames() {
        return statuses.keySet();
    }

    /**
     * @param name the name of the {@linkplain IMinecraftStatus} to check.
     * @return {@code true} if the {@linkplain IMinecraftStatus} exists, {@code false} otherwise.
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the name is blank.
     */
    public boolean doesStatusExist(String name) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Status name may not be blank.");

        return statuses.containsKey(name);
    }

    /**
     * @param name the name of the requested {@linkplain IMinecraftStatus}.
     * @return the {@link IMinecraftStatus} associated with this name.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if the name is blank or if there is no {@linkplain IMinecraftStatus} associated with this name.
     */
    public IMinecraftStatus getStatus(String name) {
        Objects.requireNonNull(name);
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Status name may not be blank.");

        if (!statuses.containsKey(name))
            throw new IllegalArgumentException("Status '" + name + "' does not exist.");

        return statuses.get(name);
    }
}
