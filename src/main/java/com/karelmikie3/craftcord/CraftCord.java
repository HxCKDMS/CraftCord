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

package com.karelmikie3.craftcord;

import com.karelmikie3.craftcord.api.Globals;
import com.karelmikie3.craftcord.api.presence.PresenceRegistry;
import com.karelmikie3.craftcord.api.status.StatusRegistry;
import com.karelmikie3.craftcord.config.Config;
import com.karelmikie3.craftcord.discord.DiscordHandler;
import com.karelmikie3.craftcord.discord.MinecraftPresence;
import com.karelmikie3.craftcord.discord.MinecraftStatus;
import com.karelmikie3.craftcord.event.EntityEvents;
import com.karelmikie3.craftcord.event.chat.ClientChatEvents;
import com.karelmikie3.craftcord.event.chat.ServerChatEvents;
import com.karelmikie3.craftcord.network.RequestEmoteMessageC2S;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import com.karelmikie3.craftcord.proxy.ClientProxy;
import com.karelmikie3.craftcord.proxy.IProxy;
import com.karelmikie3.craftcord.proxy.ServerProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.karelmikie3.craftcord.CraftCord.MOD_ID;

//TODO: keep emotes on disk instead of in memory and add option to just use memory.
//TODO: replace '[DISCORD]' in chat with the Discord logo.
@Mod(MOD_ID)
public class CraftCord {
    static final String MOD_ID = "craftcord";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static CraftCord INSTANCE;
    public final DiscordHandler DISCORD_HANDLER;
    private static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static CraftCord getInstance() {
        return INSTANCE;
    }

    public CraftCord() {
        INSTANCE = this;
        DISCORD_HANDLER = new DiscordHandler();
        Globals.PRESENCE_REGISTRY = new PresenceRegistry();
        Globals.STATUS_REGISTRY = new StatusRegistry();
        proxy.constructor();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EntityEvents());
        MinecraftForge.EVENT_BUS.register(DISCORD_HANDLER);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::initClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        Globals.PRESENCE_REGISTRY.registerPresence(MinecraftPresence.class);
        Globals.STATUS_REGISTRY.registerStatus(MinecraftStatus.class);
        Config.initConfigs();
    }

    private void setup(final FMLCommonSetupEvent event) {
        proxy.setup();
        LOGGER.info("Setting up.");
        int id = 0;
        NETWORK.messageBuilder(SendEmotesMessageS2C.class, id++)
                .encoder(SendEmotesMessageS2C::encode)
                .decoder(SendEmotesMessageS2C::decode)
                .consumer(SendEmotesMessageS2C::handle)
                .add();

        //noinspection UnusedAssignment
        NETWORK.messageBuilder(RequestEmoteMessageC2S.class, id++)
                .encoder(RequestEmoteMessageC2S::encode)
                .decoder(RequestEmoteMessageC2S::decode)
                .consumer(RequestEmoteMessageC2S::handle)
                .add();
    }

    private void initClient(final FMLClientSetupEvent event) {
        LOGGER.info("Initializing client.");
        MinecraftForge.EVENT_BUS.register(new ClientChatEvents());
    }

    private ServerChatEvents serverChatEvents;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

        serverChatEvents = new ServerChatEvents(DISCORD_HANDLER);
        MinecraftForge.EVENT_BUS.register(serverChatEvents);
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerStopping(FMLServerStoppingEvent event) {
        MinecraftForge.EVENT_BUS.unregister(serverChatEvents);
        serverChatEvents = null;
    }
}
