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

    public void insertTemplate(UUID id, ITemplate template) {
        this.map.put(id, template);
    }

    public void clear() {
        this.map.clear();
    }

    private UUID requestFreeId() {
        UUID res = UUID.randomUUID();
        return map.containsKey(res) ? requestFreeId() : res;
    }
}
