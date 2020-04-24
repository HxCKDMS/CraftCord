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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public final class StatusPresenceHelper {
    public static String getTimeOfDay(long ticks) {
        int dayTicks = (int) (ticks % 24000);
        int pHours = dayTicks / 1000 + 6;
        int hours = pHours >= 24 ? pHours - 24 : pHours;
        int minutes = MathHelper.floor((dayTicks % 1000F) / (16F + 6F/9F));

        return String.format("%02d:%02d", hours, minutes);
    }

    private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("########0.000");
    public static String getMeanTPS(MinecraftServer server) {

        double average = MathHelper.average(server.tickTimeArray);
        double tickTime = average * 1.0E-6D;
        double tps = Math.min(1000.0/tickTime, 20);
        return TIME_FORMATTER.format(tps);
    }

    public static String getAllMeanTPSAndTickTime(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        for (DimensionType dim : DimensionType.getAll()) {
            long[] times = server.getTickTime(dim);

            if (times != null) {
                double average = MathHelper.average(times);
                double tickTime = average * 1.0E-6D;
                double tps = Math.min(1000.0 / tickTime, 20);

                builder.append(StringUtils.capitalize(dim.getRegistryName() != null ? dim.getRegistryName().getPath() : String.valueOf(DimensionType.getKey(dim)))).append(" ");
                builder.append("Tick time: ").append(TIME_FORMATTER.format(tickTime)).append("ms, ");
                builder.append("TPS: ").append(TIME_FORMATTER.format(tps)).append("\n");
            }
        }

        double average = MathHelper.average(server.tickTimeArray);
        double tickTime = average * 1.0E-6D;
        double tps = Math.min(1000.0/tickTime, 20);

        builder.append("Server Tick time: ").append(TIME_FORMATTER.format(tickTime)).append("ms")
                .append(", TPS: ").append(TIME_FORMATTER.format(tps));
        return builder.toString();
    }

    public static String getPlayerList(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
            builder.append(DiscordHelper.stripFormattingForDiscord(player.getDisplayName().getFormattedText()));
            DimensionType dim = player.dimension;
            builder.append(", dimension: ").append(dim.getRegistryName() != null ? dim.getRegistryName().getPath() : String.valueOf(DimensionType.getKey(dim)));
            builder.append(", ping: ").append(player.ping).append("ms.").append('\n');
        }

        if (builder.length() > 0)
            builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    public static String getScoreboard(MinecraftServer server) {
        ScoreObjective objective = server.getScoreboard().getObjectiveInDisplaySlot(1);
        if (objective == null)
            return "There are no objectives.";

        StringBuilder builder = new StringBuilder();
        for (Score score : server.getScoreboard().getSortedScores(objective)) {
            builder.append(score.getPlayerName()).append("\t-\t").append(score.getScorePoints()).append('\n');
        }

        if (builder.length() > 0)
            builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    public static String getMobsAndCap(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        for (ServerWorld world : server.getWorlds()) {
            DimensionType dim = world.dimension.getType();
            builder.append(StringUtils.capitalize(dim.getRegistryName() != null ? dim.getRegistryName().getPath() : String.valueOf(DimensionType.getKey(dim)))).append(":\n");
            Object2IntMap<EntityClassification> map = world.countEntities();
            for (EntityClassification value : EntityClassification.values()) {
                builder.append(value.name()).append(": ").append(map.getInt(value)).append("/");
                builder.append(value.getMaxNumberOfCreature() * world.getChunkProvider().ticketManager.getSpawningChunksCount() / 289);
                builder.append('\n');
            }
        }

        if (builder.length() > 0)
            builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}
