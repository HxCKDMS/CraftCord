package com.karelmikie3.craftcord.event;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.network.SendEmotesMessageS2C;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class EntityEvents {
    /*@SubscribeEvent
    //not triggering on playing joining server-side, cause: forge bug.
    //TODO: implement this once Forge bug has been fixed.
    public void joinWorldEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity entity = (ServerPlayerEntity) event.getEntity();

        }
    }*/

    /*public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {

        }
    }*/

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

            CraftCord.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new SendEmotesMessageS2C(true));
        }
    }
}
