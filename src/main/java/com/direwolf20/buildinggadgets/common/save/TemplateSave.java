package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException;
import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.direwolf20.buildinggadgets.api.template.TemplateIO;
import com.direwolf20.buildinggadgets.common.save.TemplateSave.TemplateInfo;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public final class TemplateSave extends TimedDataSave<TemplateInfo> {

    public TemplateSave(String name) {
        super(name);
    }

    public ITemplate getTemplate(UUID id) {
        TemplateInfo info = get(id);
        info.updateTime();
        return info.getTemplate();
    }

    public void removeTemplate(UUID id) {
        remove(id);
    }

    @Override
    protected TemplateInfo createValue() {
        return new TemplateInfo();
    }

    @Override
    protected TemplateInfo readValue(CompoundNBT nbt) {
        try {
            return new TemplateInfo(nbt);
        } catch (IllegalTemplateFormatException e) {
            throw new RuntimeException("Failed to read Template! This should not have been possible!");
        }
    }

    static final class TemplateInfo extends TimedValue {
        private final ITemplate template;

        private TemplateInfo(CompoundNBT nbt) throws IllegalTemplateFormatException {
            super(nbt);
            template = TemplateIO.readTemplate(nbt, null, true);
        }

        private TemplateInfo(long lastUpdateTime, ITemplate template) {
            super(lastUpdateTime);
            this.template = template;
        }

        private TemplateInfo() {
            super();
            this.template = new DelegatingTemplate(ImmutableTemplate.create());
        }

        private ITemplate getTemplate() {
            return template;
        }

        @Override
        public CompoundNBT write() {
            return super.write();
        }
    }

}
