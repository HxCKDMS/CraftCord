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

package com.karelmikie3.craftcord.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.OptionalDouble;

public final class TimeHelper {
    public static String getTimeOfDay(long ticks) {
        int dayTicks = (int) (ticks % 24000);
        int pHours = dayTicks / 1000 + 6;
        int hours = pHours >= 24 ? pHours - 24 : pHours;
        int minutes = MathHelper.floor((dayTicks % 1000F) / (16F + 6F/9F));

        return String.format("%02d:%02d", hours, minutes);
    }

    private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("#0.000");
    public static String getMeanTPS(MinecraftServer server) {
        OptionalDouble optional = Arrays.stream(server.tickTimeArray).average();

        if (optional.isPresent()) {
            double tickTime = optional.getAsDouble() * 1.0E-6D;
            double tps = Math.min(1000.0/tickTime, 20);
            return TIME_FORMATTER.format(tps);
        } else {
            return "NO TICK TIME AVERAGE!";
        }
    }
}
