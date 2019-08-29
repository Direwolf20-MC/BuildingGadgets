package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.template.ITemplate;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class DelegatingTemplateKey implements ITemplateKey {
    public static Builder builder() {
        return new Builder();
    }

    private final ITemplateKey other;
    private final Predicate<ITemplate> requestUpdate;
    private final Predicate<ITemplate> requestRemoteUpdate;

    private DelegatingTemplateKey(ITemplateKey other, Predicate<ITemplate> requestUpdate, Predicate<ITemplate> requestRemoteUpdate) {
        this.other = other;
        this.requestUpdate = requestUpdate;
        this.requestRemoteUpdate = requestRemoteUpdate;
    }

    @Override
    public UUID getTemplateId(Supplier<UUID> freeIdAllocator) {
        return other.getTemplateId(freeIdAllocator);
    }

    @Override
    public ITemplate createTemplate(UUID id) {
        return other.createTemplate(id);
    }

    @Override
    public boolean requestUpdate(ITemplate currentTemplate) {
        return requestUpdate.test(currentTemplate);
    }

    @Override
    public boolean requestRemoteUpdate(ITemplate currentTemplate) {
        return requestRemoteUpdate.test(currentTemplate);
    }

    public static final class Builder {
        private ITemplateKey other;
        private Predicate<ITemplate> requestUpdate;
        private Predicate<ITemplate> requestRemoteUpdate;

        private Builder() {
        }

        public Builder withRequestClientUpdatePredicate(Predicate<ITemplate> requestUpdate) {
            this.requestUpdate = Objects.requireNonNull(requestUpdate);
            return this;
        }

        public Builder withRequestServerUpdatePredicate(Predicate<ITemplate> requestServerUpdate) {
            this.requestRemoteUpdate = Objects.requireNonNull(requestServerUpdate);
            return this;
        }

        public DelegatingTemplateKey build(ITemplateKey other) {
            return new DelegatingTemplateKey(
                    Objects.requireNonNull(other),
                    requestUpdate != null ? requestUpdate : other::requestUpdate,
                    requestRemoteUpdate != null ? requestRemoteUpdate : other::requestRemoteUpdate);
        }
    }
}
