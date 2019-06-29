package com.karelmikie3.craftcord.patch;

import com.karelmikie3.craftcord.util.EmoteHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class NewChatGuiPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    @SuppressWarnings("unused")
    public static String addEmotes(String text, float y) {
        if (text != null && !text.isEmpty()) {
            List<String> emotes = EmoteHelper.getOrderedEmotes(text);
            for (String emote : emotes) {
                String[] parts = text.split(":" + emote + ":", 2);
                text = parts[0] + "  " + parts[1];
                int x = mc.fontRenderer.getStringWidth(parts[0]);
                renderEmote(x, y, new ResourceLocation("craftcord", "textures/emotedata/" + EmoteHelper.getEmoteID(emote)));
            }
        }
        return text;
    }

    private static void renderEmote(float x, float y, ResourceLocation emote) {
        GlStateManager.pushMatrix();
        {
            GlStateManager.color3f(1, 1, 1);
            GlStateManager.translatef(x, y, 0);
            float scalar = ((float) mc.fontRenderer.FONT_HEIGHT) / 128F;
            GlStateManager.scalef(scalar, scalar, 1F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();

            builder.begin(7, DefaultVertexFormats.POSITION_TEX);
            mc.getTextureManager().bindTexture(emote);
            builder.pos(0, 128, 0).tex(0, 1).endVertex();
            builder.pos(128, 128, 0).tex(1, 1).endVertex();
            builder.pos(128, 0, 0).tex(1, 0).endVertex();
            builder.pos(0, 0, 0).tex(0, 0).endVertex();


            tessellator.draw();
        }
        GlStateManager.popMatrix();
    }
}
