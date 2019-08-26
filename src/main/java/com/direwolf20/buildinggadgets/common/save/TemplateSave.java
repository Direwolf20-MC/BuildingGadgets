package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.items.ITemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TemplateSave {
    private final Map<UUID, ITemplate> idToTemplate;

    public TemplateSave() {
        idToTemplate = new HashMap<>();
    }
}
