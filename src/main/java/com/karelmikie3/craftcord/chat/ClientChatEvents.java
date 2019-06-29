package com.karelmikie3.craftcord.chat;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientChatEvents {
    private static final ResourceLocation resourceLocation = new ResourceLocation("craftcord", "textures/375790194936512512.png");

    /*@SubscribeEvent
    public static void renderChat(RenderGameOverlayEvent.Chat event) {
        //System.out.println("event.getPosX() = " + event.getPosX());
        //System.out.println("event.getPosY() = " + event.getPosY());
        //System.out.println(resourceLocation);
        GL11.glPushMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(resourceLocation);
        //Minecraft.getInstance().ingameGUI.blit(20, 20, 0, 0, 256, 256);
        IngameGui.blit(9, event.getPosY() - 100, 0, 0, 8, 8, 8, 8);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.translated(event.getPosX(), event.getPosY() - 100, 0);
        GlStateManager.scaled(0.125, 0.125, 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(9, DefaultVertexFormats.POSITION_TEX);
        builder.pos(0, 64, 0).tex(0, 1).endVertex();
        builder.pos(64, 64, 0).tex(1, 1).endVertex();
        builder.pos(64, 0, 0).tex(1, 0).endVertex();
        builder.pos(0, 0, 0).tex(0, 0).endVertex();
        //Minecraft.getInstance().ingameGUI.getChatGUI().clearChatMessages();


        tessellator.draw();
        GL11.glPopMatrix();
    }*/
}
