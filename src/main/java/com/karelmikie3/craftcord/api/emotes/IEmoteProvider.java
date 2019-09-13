package com.karelmikie3.craftcord.api.emotes;

import java.io.InputStream;
import java.util.Set;

public interface IEmoteProvider {
    void prepare(long emoteID, String displayName, boolean usable, boolean animated);
    InputStream getInput(long emoteID, boolean metadata);
    boolean exists(long emoteID);
    boolean exists(String displayName);
    long getEmoteID(String displayName);
    boolean canClientUse(String displayName);
    void requestFromServer(long emoteID);
    Set<String> usableEmotes();
}
