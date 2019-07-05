package com.karelmikie3.craftcord.patch;

import com.google.common.collect.Lists;
import com.karelmikie3.craftcord.util.ClientEmoteHelper;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

@OnlyIn(Dist.CLIENT)
public final class NewChatGuiPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    @SuppressWarnings("unused")
    public static String addEmotes(String text, float y) {
        if (text != null && !text.isEmpty()) {
            for (String emoteID : CommonEmoteHelper.getOrderedEmotes(text, s -> true)) {
                if (ClientEmoteHelper.hasEmoteData(emoteID)) {
                    String[] parts = text.split(":" + emoteID + ":", 2);
                    text = parts[0] + "   " + parts[1];
                    int x = mc.fontRenderer.getStringWidth(parts[0]);

                    renderEmote(x, y, new ResourceLocation("craftcord", "textures/emotedata/" + emoteID));
                } else {
//                    System.out.println("requesting download.");
                    //request downloading emote with this id

                    ClientEmoteHelper.requestEmote(emoteID);
                }
            }
        }
        return text;
    }

    private static Queue<String> emotesToAdd = new LinkedTransferQueue<>();

    @SuppressWarnings("unused")
    public static ITextComponent removeEmotes(ITextComponent component) {
        emotesToAdd.clear();

        ITextComponent newComponent = new StringTextComponent("");

        List<ITextComponent> components = Lists.newArrayList(component);

        for (ITextComponent iTextComponent : components) {
            String text = iTextComponent.getUnformattedComponentText();

            emotesToAdd.addAll(CommonEmoteHelper.getOrderedEmotes(text, ClientEmoteHelper::hasEmoteData));

            for (String emote : CommonEmoteHelper.getOrderedEmotes(text, ClientEmoteHelper::hasEmoteData)) {
                String[] parts = text.split(":" + emote + ":", 2);
                text = parts[0] + "\u200ba" + parts[1];
            }

            newComponent.appendSibling(new StringTextComponent(text).setStyle(iTextComponent.getStyle().createDeepCopy()));
        }

        return newComponent;
    }

    @SuppressWarnings("unused")
    public static List<ITextComponent> changeList(List<ITextComponent> components) {
        if (emotesToAdd.isEmpty())
            return components;

        List<ITextComponent> list = Lists.newArrayList();

        String addToNext = "";
        for (ITextComponent outerComponent : components) {
            ITextComponent newComponent = new StringTextComponent("");
            for (ITextComponent component : Lists.newArrayList(outerComponent)) {
                String text = addToNext + component.getUnformattedComponentText();
                addToNext = "";

                while (text.contains("\u200ba") && emotesToAdd.peek() != null) {
                    text = text.replaceFirst("\\u200ba", ":" + emotesToAdd.poll() + ":");
                }

                if (text.endsWith("\u200b")) {
                    text = text.substring(0, text.length() - 1);
                    addToNext = "\u200b";
                }

                newComponent.appendSibling(new StringTextComponent(text).setStyle(component.getStyle().createDeepCopy()));
            }

            list.add(newComponent);
        }

        if (list.size() != components.size()) {
            System.err.println("MAJOR PROBLEM!");
        }

        emotesToAdd.clear();
        return list;
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
