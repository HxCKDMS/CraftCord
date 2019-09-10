package com.karelmikie3.craftcord.api.emotes;

import java.io.InputStream;

public interface IEmoteProvider {
    void prepare(long emoteid, boolean animated);
    InputStream getInput(long emoteid);
    boolean exists(long emoteid);
}
