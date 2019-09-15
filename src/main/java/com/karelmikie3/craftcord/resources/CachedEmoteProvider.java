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

package com.karelmikie3.craftcord.resources;

import com.karelmikie3.craftcord.api.emotes.IEmoteProvider;

import java.io.InputStream;
import java.util.Set;

public class CachedEmoteProvider implements IEmoteProvider {
    @Override
    public void prepare(long emoteID, String displayName, boolean usable, boolean animated) {

    }

    @Override
    public InputStream getInput(long emoteID, boolean metadata) {
        return null;
    }

    @Override
    public boolean exists(long emoteID) {
        return false;
    }

    @Override
    public boolean exists(String displayName) {
        return false;
    }

    @Override
    public long getEmoteID(String displayName) {
        return 0;
    }

    @Override
    public void requestFromServer(long emoteID) {

    }

    @Override
    public Set<String> usableEmotes() {
        return null;
    }
}
