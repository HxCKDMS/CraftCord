package com.karelmikie3.craftcord.chat;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientChatEvents {

    /*@SubscribeEvent
    public static void renderChat(RenderGameOverlayEvent.Chat event) {

    }*/

    @SubscribeEvent
    public static void keyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
        if (event.getGui() instanceof ChatScreen) {
            System.out.println(event.getKeyCode());
        }
    }
}
