package com.direwolf20.buildinggadgets.api.template.provider;

import com.direwolf20.buildinggadgets.api.template.ITemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class InMemoryTemplateProvider implements ITemplateProvider {
    private final Map<UUID, ITemplate> map;

    public InMemoryTemplateProvider() {
        this.map = new HashMap<>();
    }

    @Override
    public ITemplate getTemplateForKey(ITemplateKey key) {
        UUID id = key.getTemplateId(this::requestFreeId);
        return map.computeIfAbsent(id, key::createTemplate);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public void setTemplate(ITemplateKey key, ITemplate template) {
        this.map.put(key.getTemplateId(this::requestFreeId), template);
    }

    public void clear() {
        this.map.clear();
    }

    private UUID requestFreeId() {
        UUID res = UUID.randomUUID();
        return map.containsKey(res) ? requestFreeId() : res;
    }
}
