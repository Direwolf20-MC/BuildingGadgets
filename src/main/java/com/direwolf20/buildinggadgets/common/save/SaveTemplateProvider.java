package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;

import java.util.UUID;
import java.util.function.Supplier;

public final class SaveTemplateProvider implements ITemplateProvider {
    private final Supplier<TemplateSave> save;

    public SaveTemplateProvider(Supplier<TemplateSave> save) {
        this.save = save;
    }

    public TemplateSave getSave() {
        return save.get();
    }

    @Override
    public ITemplate getTemplateForKey(ITemplateKey key) {
        UUID id = key.getTemplateId(() -> {
            UUID freeId = getSave().getFreeUUID();
            onIdAllocated(freeId);
            return freeId;
        });
        ITemplate template = getSave().getTemplate(id);
        if (key.requestRemoteUpdate(template))
            onRemoteUpdateRequested(id, template);
        else if (key.requestUpdate(template))
            onUpdateRequested(id);
        return template;
    }

    public void onRemoteIdAllocated(UUID allocated) {
        getSave().getTemplate(allocated);
    }

    private void onIdAllocated(UUID allocatedId) {
        //TODO packet
    }

    private void onUpdateRequested(UUID id) {
        //TODO packet
    }

    private void onRemoteUpdateRequested(UUID id, ITemplate template) {
        //TODO packet
    }
}
