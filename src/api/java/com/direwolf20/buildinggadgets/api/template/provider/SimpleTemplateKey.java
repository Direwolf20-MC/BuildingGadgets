package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public final class SimpleTemplateKey implements ITemplateKey {
    private Supplier<ITemplate> templateFactory;
    @Nullable
    private UUID id;

    public SimpleTemplateKey() {
        this(null);
    }

    public SimpleTemplateKey(@Nullable UUID id) {
        this(DelegatingTemplate::new, id);
    }

    public SimpleTemplateKey(Supplier<ITemplate> templateFactory, @Nullable UUID id) {
        this.templateFactory = templateFactory;
        this.id = id;
    }

    @Override
    public UUID getTemplateId(Supplier<UUID> freeIdAllocator) {
        if (id == null)
            setUUID(freeIdAllocator.get());
        return id;
    }

    @Override
    public ITemplate createTemplate(UUID id) {
        return templateFactory.get();
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
