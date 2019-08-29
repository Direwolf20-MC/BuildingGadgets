package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class SimpleTemplateKey implements ITemplateKey {
    private boolean requireClientUpdate;
    private boolean requireServerUpdate;
    private Supplier<ITemplate> templateFactory;
    @Nullable
    private UUID id;

    public SimpleTemplateKey() {
        this(true, false);
    }

    public SimpleTemplateKey(boolean requireClientUpdate, boolean requireServerUpdate) {
        this(requireClientUpdate, requireServerUpdate, DelegatingTemplate::new);
    }

    public SimpleTemplateKey(boolean requireClientUpdate, boolean requireServerUpdate, Supplier<ITemplate> templateFactory) {
        this(requireClientUpdate, requireServerUpdate, templateFactory, null);
    }

    public SimpleTemplateKey(boolean requireClientUpdate, boolean requireServerUpdate, Supplier<ITemplate> templateFactory, @Nullable UUID id) {
        this.requireClientUpdate = requireClientUpdate;
        this.requireServerUpdate = requireServerUpdate;
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
    public boolean requestUpdate(ITemplate currentTemplate) {
        return requireClientUpdate();
    }

    @Override
    public boolean requestRemoteUpdate(ITemplate currentTemplate) {
        return requireServerUpdate();
    }

    @Override
    public ITemplate createTemplate(UUID id) {
        return templateFactory.get();
    }

    public SimpleTemplateKey setUUID(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    public SimpleTemplateKey setRequireClientUpdate(boolean requireClientUpdate) {
        this.requireClientUpdate = requireClientUpdate;
        return this;
    }

    public SimpleTemplateKey setRequireServerUpdate(boolean requireServerUpdate) {
        this.requireServerUpdate = requireServerUpdate;
        return this;
    }

    public boolean requireClientUpdate() {
        return requireClientUpdate;
    }

    public boolean requireServerUpdate() {
        return requireServerUpdate;
    }

    @Nullable
    public UUID getId() {
        return id;
    }
}
