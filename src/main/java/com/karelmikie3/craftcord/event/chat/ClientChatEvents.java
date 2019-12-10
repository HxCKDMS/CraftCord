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

package com.karelmikie3.craftcord.event.chat;

import com.karelmikie3.craftcord.CraftCord;
import com.karelmikie3.craftcord.util.CommonEmoteHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.codehaus.plexus.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ClientChatEvents {
    private int chatPosX, chatPosY;

    @SubscribeEvent
    public void getChatPos(RenderGameOverlayEvent.Chat event) {
        chatPosX = event.getPosX();
        chatPosY = event.getPosY();
    }

    @SubscribeEvent
    public void renderEmotes(RenderGameOverlayEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        IngameGui ingameGui = mc.ingameGUI;
        NewChatGui newChatGui = ingameGui.getChatGUI();

        int ticks = ingameGui.getTicks();

        for (int i = 0; i + newChatGui.scrollPos < newChatGui.drawnChatLines.size() && i < newChatGui.getLineCount(); i++) {
            float y = chatPosY - mc.fontRenderer.FONT_HEIGHT * i - 1;

            ChatLine line = newChatGui.drawnChatLines.get(i + newChatGui.scrollPos);
            ITextComponent component = line.getChatComponent();
            LinkedList<ITextComponent> siblings = new LinkedList<>(component.getSiblings());
            int priorLength = 0;

            do {
                if (component.getUnformattedComponentText().equals("  ")) {
                    HoverEvent hoverEvent = component.getStyle().getHoverEvent();

                    if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                        ITextComponent clickComponent = hoverEvent.getValue();
                        hoverEvent = clickComponent.getStyle().getHoverEvent();
                        if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                            clickComponent = hoverEvent.getValue();
                            String hoverText = clickComponent.getUnformattedComponentText();
                            if (StringUtils.isNumeric(hoverText)) {
                                long emoteID = Long.parseLong(hoverText);
                                if (CraftCord.getInstance().getClientDiscordHandler().emoteProvider.exists(emoteID)) {
                                    int ticksPassed = ticks - line.getUpdatedCounter();

                                    int x = chatPosX + priorLength + 2;
                                    if (ticksPassed < 200 || newChatGui.getChatOpen())
                                        renderEmote(x, y, newChatGui.getChatOpen() ? 0 : ticksPassed, new ResourceLocation("craftcordemotes", "textures/emotedata/" + emoteID));
                                } else {
                                    CraftCord.getInstance().getClientDiscordHandler().emoteProvider.requestFromServer(emoteID);
                                }
                            }
                        }
                    }
                }

                priorLength += mc.fontRenderer.getStringWidth(TextFormatting.getTextWithoutFormattingCodes(component.getUnformattedComponentText()));
            } while (!siblings.isEmpty() && (component = siblings.removeFirst()) != null);
        }
    }

//    @SubscribeEvent
//    public void renderTooltip(ItemTooltipEvent event) {
//        System.out.println("test");
//        if (event.getToolTip().size() > 0) {
//            String line = event.getToolTip().get(0).getUnformattedComponentText();
//            if (!line.isEmpty() && StringUtils.isNumeric(line)) {
//                long emoteID = Long.parseLong(line);
//                if (CraftCord.getInstance().getClientDiscordHandler().emoteProvider.exists(emoteID)) {
//                    String name = CraftCord.getInstance().getClientDiscordHandler().emoteProvider.getEmoteName(emoteID);
//                    event.getToolTip().set(0, new StringTextComponent(name));
//                }
//            }
//        }
//    }

    private static void renderEmote(float x, float y, int ticksPassed, ResourceLocation emote) {
        Minecraft mc = Minecraft.getInstance();
        double chatOpacity = mc.gameSettings.chatOpacity * 0.9D + 0.1D;
        double fadeOut = MathHelper.clamp((1 - ticksPassed / 200f) * 10D, 0D, 1D);
        double alpha = fadeOut * fadeOut * chatOpacity;


        GlStateManager.pushMatrix();
        {
            GlStateManager.enableBlend();
            GlStateManager.color4f(1, 1, 1, (float) alpha);
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
            GlStateManager.disableAlphaTest();
            GlStateManager.disableBlend();
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void clientChatReceived(ClientChatReceivedEvent event) {
    }

    @SubscribeEvent
    public void clientChatEvent(ClientChatEvent event) {
        String message = event.getMessage();

        //TODO: only do this for usable emotes
        List<String> emotes = CommonEmoteHelper.getOrderedEmotes(message, CraftCord.getInstance().getClientDiscordHandler().emoteProvider::exists);
        LinkedList<String> emoteIds = emotes.parallelStream()
                .mapToLong(CraftCord.getInstance().getClientDiscordHandler().emoteProvider::getEmoteID)
                .mapToObj(Long::toString)
                .collect(Collectors.toCollection(LinkedList::new));

        for (String emote : emotes) {
            message = message.replaceFirst(":" + emote + ":", ":" + emoteIds.removeFirst() + ":");
        }

        event.setMessage(message);
    }

}
