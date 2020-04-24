package com.karelmikie3.craftcord.resources;

import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MemoryEmoteProvider extends AbstractEmoteProvider {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Object LOCK = new Object();

    //TODO: clear these maps on logout of client.
    private final Map<Long, byte[]> emotes = new HashMap<>();
    private final Map<Long, byte[]> metadata = new HashMap<>();

    //https://cdn.discordapp.com/emojis/537722102954983466.png
    private final String URLTemplate;

    public MemoryEmoteProvider(String URLTemplate) {
        this.URLTemplate = URLTemplate;
    }

    public MemoryEmoteProvider() {
        this("https://cdn.discordapp.com/emojis/%d.%s");
    }

    protected void prepareASync(long emoteID, String displayName, boolean usable, boolean animated) {
        String urlString = String.format(this.URLTemplate, emoteID, animated ? "gif" : "png");

        Pair<byte[], byte[]> dataMetadataPair = processEmote(urlString, animated);

        if (dataMetadataPair == null)
            return;

        synchronized (LOCK) {
            this.emotes.put(emoteID, dataMetadataPair.getKey());
            this.metadata.put(emoteID, dataMetadataPair.getValue());
            this.displayNameToID.put(displayName, emoteID);

            if (usable)
                this.usable.add(displayName);
        }

        ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);

        executor.runImmediately(() -> {
            ResourceLocation emoteResource = new ResourceLocation("craftcordemotes", "textures/emotedata/" + emoteID);
            mc.getTextureManager().loadTexture(emoteResource, new EmoteTexture(emoteResource));
        });
    }

    @Override
    public InputStream getInput(long emoteID, boolean metadata) {
        return new ByteArrayInputStream(metadata ? this.metadata.get(emoteID) : emotes.get(emoteID));
    }

    @Override
    public boolean exists(long emoteID) {
        return emotes.containsKey(emoteID);
    }
}
