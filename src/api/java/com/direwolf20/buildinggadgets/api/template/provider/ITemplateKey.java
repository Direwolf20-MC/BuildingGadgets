package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.template.ITemplate;

import java.util.UUID;
import java.util.function.Supplier;

public interface ITemplateKey {
    UUID getTemplateId(Supplier<UUID> freeIdAllocator);

    ITemplate createTemplate(UUID id);
}
