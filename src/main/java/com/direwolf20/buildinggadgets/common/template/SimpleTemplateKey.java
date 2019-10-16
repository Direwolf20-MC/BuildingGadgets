package com.direwolf20.buildinggadgets.common.template;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public final class SimpleTemplateKey implements ITemplateKey {
    @Nullable
    private UUID id;

    public SimpleTemplateKey() {
        this(null);
    }

    public SimpleTemplateKey(@Nullable UUID id) {
        this.id = id;
    }

    @Override
    public UUID getTemplateId(Supplier<UUID> freeIdAllocator) {
        if (id == null)
            setUUID(freeIdAllocator.get());
        return id;
    }

    @Nullable
    public UUID getId() {
        return id;
    }

    public SimpleTemplateKey setUUID(@Nullable UUID id) {
        this.id = id;
        return this;
    }
}
