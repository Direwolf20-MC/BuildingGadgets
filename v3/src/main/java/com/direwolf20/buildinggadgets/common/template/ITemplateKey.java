package com.direwolf20.buildinggadgets.common.template;

import java.util.UUID;
import java.util.function.Supplier;

public interface ITemplateKey {
    UUID getTemplateId(Supplier<UUID> freeIdAllocator);
}
