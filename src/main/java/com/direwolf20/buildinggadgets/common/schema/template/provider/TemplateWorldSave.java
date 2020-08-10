package com.direwolf20.buildinggadgets.common.schema.template.provider;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.schema.template.Template;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TemplateWorldSave extends WorldSavedData {
    public static TemplateWorldSave getInstance(ServerWorld world) {
        return world.getSavedData().getOrCreate(TemplateWorldSave::new, NAME);
    }

    private static final String NAME = BuildingGadgets.MOD_ID + "_template_data";
    private static final String KEY_TEMPLATE_LIST = "templates";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";
    private final Map<UUID, Template> templates;

    public TemplateWorldSave() {
        super(NAME);
        this.templates = new HashMap<>();
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (! nbt.contains(KEY_TEMPLATE_LIST, NBT.TAG_LIST)) {
            BuildingGadgets.LOGGER.warn("No templates found to load from nbt data.");
            return;
        }

        ListNBT templates = nbt.getList(KEY_TEMPLATE_LIST, NBT.TAG_COMPOUND);
        BuildingGadgets.LOGGER.debug("Loading {} template{}.", templates.size(), getSExtension(templates.size()));

        int loadCount = 0;
        for (INBT templateNBT : templates) {
            CompoundNBT templateCompound = (CompoundNBT) templateNBT;
            UUID id = templateCompound.getUniqueId(KEY_ID);
            boolean deserializeError = Template.deserializeNBT(templateCompound.getCompound(KEY_DATA))
                    .map(t -> {
                        this.templates.put(id, t);
                        return Boolean.FALSE;
                    })
                    .orElse(Boolean.TRUE);
            if (deserializeError)
                BuildingGadgets.LOGGER.error("Could not load Template with id {}. Discarding.", id);
            else
                loadCount++;
        }

        if (loadCount == templates.size())
            BuildingGadgets.LOGGER.debug("Successfully loaded {}/{} template{}.",
                    templates.size(), templates.size(), getSExtension(templates.size()));
        else
            BuildingGadgets.LOGGER.warn("Loaded {}/{} template{} successfully. {} template{} could not be loaded!",
                    loadCount, templates.size(), getSExtension(templates.size()), templates.size() - loadCount,
                    getSExtension(templates.size() - loadCount));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        BuildingGadgets.LOGGER.debug("Saving {} template{}.", templates.size(), getSExtension(templates.size()));

        ListNBT templateList = new ListNBT();
        for (Map.Entry<UUID, Template> entry : templates.entrySet()) {
            CompoundNBT templateCompound = new CompoundNBT();
            templateCompound.putUniqueId(KEY_ID, entry.getKey());
            templateCompound.put(KEY_DATA, entry.getValue().serializeNBT());
            templateList.add(templateCompound);
        }

        compound.put(KEY_TEMPLATE_LIST, templateList);

        return compound;
    }

    public Optional<Template> getTemplate(UUID id) {//Templates are immutable => no markDirty
        return Optional.ofNullable(templates.get(id));
    }

    public void setTemplate(UUID id, @Nullable Template template) {
        if (template != null)
            templates.put(id, template);
        else
            templates.remove(id);

        markDirty();
    }

    private String getSExtension(int number) {
        return number == 1 ? "" : "s";
    }
}
