package com.karelmikie3.craftcord.event;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntityEvents {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            final ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            CraftCord.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new SendEmotesMessageS2C(true));
        }
    }
}
