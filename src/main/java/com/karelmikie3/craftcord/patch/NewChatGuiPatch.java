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

package com.karelmikie3.craftcord.patch;

import com.google.common.collect.Lists;
import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.config.Config;
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
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused")
public final class NewChatGuiPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Predicate<String> emoteTest = s -> {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    private static Predicate<String> emoteStrip = s -> CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.exists(Long.parseLong(s));

    public static String addEmotes(String text, float y) {
        if (Config.emoteRenderingEnabled() && text != null && !text.isEmpty()) {
            for (String emoteIDString : CommonEmoteHelper.getOrderedEmotes(text, emoteTest)) {
                long emoteID = Long.parseLong(emoteIDString);

                if (CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.exists(emoteID)) {
                    String[] parts = text.split(":" + emoteID + ":", 2);
                    text = parts[0] + "   " + parts[1];
                    int x = mc.fontRenderer.getStringWidth(parts[0]);

                    renderEmote(x, y, new ResourceLocation("craftcordemotes", "textures/emotedata/" + emoteID));
                } else {
                    CraftCord.getInstance().CLIENT_DISCORD_HANDLER.emoteProvider.requestFromServer(emoteID);
                }
            }
        }
        return text;
    }

    private static Queue<String> emotesToAdd = new LinkedTransferQueue<>();
    //TODO: redo all of this without placeholders.
    public static ITextComponent removeEmotes(ITextComponent component) {
        if (!Config.emoteRenderingEnabled())
            return component;

        emotesToAdd.clear();

        ITextComponent newComponent = new StringTextComponent("");

        List<ITextComponent> components = Lists.newArrayList(component);

        for (ITextComponent iTextComponent : components) {
            String text = iTextComponent.getUnformattedComponentText();

            emotesToAdd.addAll(CommonEmoteHelper.getOrderedEmotes(text, emoteTest.and(emoteStrip)));

            for (String emote : CommonEmoteHelper.getOrderedEmotes(text, emoteTest.and(emoteStrip))) {
                String[] parts = text.split(":" + emote + ":", 2);
                text = parts[0] + "\u200ba" + parts[1];
            }

            newComponent.appendSibling(new StringTextComponent(text).setStyle(iTextComponent.getStyle().createDeepCopy()));
        }

        return newComponent;
    }

    public static List<ITextComponent> changeList(List<ITextComponent> components) {
        if (!Config.emoteRenderingEnabled() || emotesToAdd.isEmpty())
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
            //mc.getTextureManager().loadTickableTexture(emote, new AtlasTexture("test"));
            //mc.getTextureManager().loadTickableTexture(emote, new EmoteTexture(emote));
            builder.pos(0, 128, 0).tex(0, 1).endVertex();
            builder.pos(128, 128, 0).tex(1, 1).endVertex();
            builder.pos(128, 0, 0).tex(1, 0).endVertex();
            builder.pos(0, 0, 0).tex(0, 0).endVertex();


            tessellator.draw();
        }
        GlStateManager.popMatrix();
    }
}
