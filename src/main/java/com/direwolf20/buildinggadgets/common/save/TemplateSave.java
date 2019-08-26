package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.direwolf20.buildinggadgets.api.template.TemplateIO;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class TemplateSave {
    private final Map<UUID, TemplateInfo> idToTemplate;
    private final Path saveFolder;
    private final Supplier<ITemplate> templateFactory;

    public TemplateSave(Path saveFolder) {
        this(saveFolder, () -> new DelegatingTemplate(ImmutableTemplate.create()));
    }

    public TemplateSave(Path saveFolder, Supplier<ITemplate> templateFactory) {
        idToTemplate = new HashMap<>();
        this.saveFolder = Objects.requireNonNull(saveFolder);
        this.templateFactory = templateFactory;
    }

    public UUID getFreeUUID() {
        UUID res = UUID.randomUUID();
        return idToTemplate.containsKey(res) ? getFreeUUID() : res;
    }

    public ITemplate getTemplate(UUID id) {
        return idToTemplate
                .computeIfAbsent(id, uuid -> new TemplateInfo(templateFactory.get()))
                .saveRequired() //it was accessed - we should save it...
                .getTemplate();
    }

    public void saveAll() {
        saveAll(true);
    }

    public void saveAll(boolean requiredOnly) {
        try {
            if (! Files.exists(saveFolder))
                Files.createDirectories(saveFolder);
        } catch (IOException e) {
            BuildingGadgets.LOG.error("Failed to create Directory {}. Cannot save this TemplateSave!", saveFolder);
            return;
        }
        for (Entry<UUID, TemplateInfo> entry : idToTemplate.entrySet()) {
            if (! requiredOnly || entry.getValue().isSaveRequired())
                saveTemplateInfo(entry.getKey(), entry.getValue());
        }
    }

    private void saveTemplateInfo(UUID id, TemplateInfo templateInfo) {
        String fileName = id.toString();
        Path templateFile = saveFolder.resolve(fileName + Reference.TEMPLATE_FILE_ENDING);
        Path templateHeaderFile = saveFolder.resolve(fileName + Reference.TEMPLATE_HEADER_FILE_ENDING);
        try {
            if (! Files.exists(templateFile))
                Files.createFile(templateFile);
            try (OutputStream templateFileStream = Files.newOutputStream(templateFile)) {
                templateInfo.writeTemplateTo(templateFileStream);
            }
        } catch (IOException e) {
            BuildingGadgets.LOG.error("Failed to save {}{}!", id, Reference.TEMPLATE_FILE_ENDING, e);
        }
        try {
            if (! Files.exists(templateHeaderFile))
                Files.createFile(templateHeaderFile);
            try (OutputStream templateHeaderFileStream = Files.newOutputStream(templateHeaderFile)) {
                templateInfo.writeTemplateHeaderTo(templateHeaderFileStream);
            }
        } catch (IOException e) {
            BuildingGadgets.LOG.error("Failed to save {}{}!", id, Reference.TEMPLATE_HEADER_FILE_ENDING, e);
        }
    }

    public void loadAll() {

    }

    private static final class TemplateInfo {
        private final ITemplate template;
        private boolean saveRequired;

        private TemplateInfo(ITemplate template) {
            this.template = template;
            this.saveRequired = false;
        }

        private ITemplate getTemplate() {
            return template;
        }

        private boolean isSaveRequired() {
            return saveRequired;
        }

        private TemplateInfo saveRequired() {
            this.saveRequired = true;
            return this;
        }

        private void writeTemplateTo(OutputStream stream) throws IOException {
            TemplateIO.writeTemplate(template, stream);
        }

        private void writeTemplateHeaderTo(OutputStream stream) throws IOException {
            TemplateIO.writeTemplateHeaderJson(template, stream);
        }
    }

    private final class ReadTemplateVisitor extends SimpleFileVisitor<Path> {
        
    }
}
