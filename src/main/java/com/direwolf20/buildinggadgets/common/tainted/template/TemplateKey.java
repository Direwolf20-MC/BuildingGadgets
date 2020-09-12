package com.direwolf20.buildinggadgets.common.tainted.template;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A very simple {@link ITemplateKey} which allows to query an {@link ITemplateProvider} for a specific Template, without
 * having the CapabilityProvider at hand. (For example useful for packets)
 */
public final class TemplateKey implements ITemplateKey {
    @Nullable
    private UUID id;

    public TemplateKey() {
        this(null);
    }

    public TemplateKey(@Nullable UUID id) {
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

    public TemplateKey setUUID(@Nullable UUID id) {
        this.id = id;
        return this;
    }
}
