package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.save.TemplateSave.TemplateInfo;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundNBT;

import java.util.UUID;

public final class TemplateSave extends TimedDataSave<TemplateInfo> {

    public TemplateSave(String name) {
        super(name);
    }

    public Template getTemplate(UUID id) {
        TemplateInfo info = get(id, uuid -> new TemplateInfo());
        return markDirtyAndUpdate(info).getTemplate();
    }

    void setTemplate(UUID id, Template template) {
        markDirtyAndUpdate(get(id, uuid -> new TemplateInfo(template))).setTemplate(template);
    }

    void removeTemplate(UUID id) {
        remove(id);
        markDirty();
    }

    @Override
    protected TemplateInfo createValue() {
        return new TemplateInfo();
    }

    @Override
    protected TemplateInfo readValue(CompoundNBT nbt) {
        return new TemplateInfo(nbt);
    }

    private TemplateInfo markDirtyAndUpdate(TemplateInfo info) {
        markDirty();
        return info.updateTime();
    }

    static final class TemplateInfo extends TimedDataSave.TimedValue { //for reasons I don't understand it doesn't compile if you leave the TimedDataSave out!
        private Template template;

        private TemplateInfo(CompoundNBT nbt) {
            super(nbt);
            template = Template.deserialize(nbt.getCompound(NBTKeys.KEY_DATA), null, true);
        }

        private TemplateInfo(Template template) {
            super();
            this.template = template;
        }

        private TemplateInfo(long lastUpdateTime, Template template) {
            super(lastUpdateTime);
            this.template = template;
        }

        private TemplateInfo() {
            this(new Template());
        }

        private Template getTemplate() {
            return template;
        }

        public TemplateInfo setTemplate(Template template) {
            this.template = template;
            return this;
        }

        @Override
        public TemplateInfo updateTime() {
            return (TemplateInfo) super.updateTime();
        }

        @Override
        public CompoundNBT write() {
            CompoundNBT nbt = super.write();
            nbt.put(NBTKeys.KEY_DATA, template.serialize(true));
            return nbt;
        }
    }

}
