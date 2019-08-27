package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.DelegatingTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.ImmutableTemplate;
import com.direwolf20.buildinggadgets.api.template.TemplateIO;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
        Path templateFile = saveFolder.resolve(fileName + Reference.FILE_ENDING_TEMPLATE);
        Path templateHeaderFile = saveFolder.resolve(fileName + Reference.FILE_ENDING_TEMPLATE_HEADER);
        templateInfo.saveTo(id, templateFile, templateHeaderFile);
    }

    public void loadAll() {
        try {
            Files.walkFileTree(saveFolder, new ReadTemplateVisitor());
        } catch (IOException e) {
            BuildingGadgets.LOG.error("An error occurred whilst loading Templates from {}. Loading was aborted!", saveFolder);
        }
    }

    public void clear() {
        idToTemplate.clear();
    }

    private void loadDataForId(UUID id, Path templateFile, Path headerFile) throws IOException {
        TemplateHeader header = loadHeader(headerFile);
        ITemplate template = loadTemplate(templateFile, header);
        idToTemplate.put(id, new TemplateInfo(template));
        BuildingGadgets.LOG.trace("Successfully loaded Template with id={} and {}.", id, header != null ? "Template Header" : "no Header");
    }

    @Nullable
    private TemplateHeader loadHeader(Path headerFile) throws IOException {
        if (headerFile != null) {
            try (InputStream stream = Files.newInputStream(headerFile)) {
                return TemplateIO.readTemplateHeaderJson(stream);
            }
        }
        return null;
    }

    private ITemplate loadTemplate(Path templateFile, @Nullable TemplateHeader header) throws IOException {
        try (InputStream stream = Files.newInputStream(templateFile)) {
            return TemplateIO.readTemplate(stream, header);
        }
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

        private void saveTo(UUID id, Path templateFile, Path templateHeaderFile) {
            writeTemplateTo(id, templateFile);
            writeTemplateHeaderTo(id, templateHeaderFile);
            this.saveRequired = false;
        }

        private void writeTemplateTo(UUID id, Path templateFile) {
            try {
                if (! Files.exists(templateFile))
                    Files.createFile(templateFile);
                try (OutputStream stream = Files.newOutputStream(templateFile)) {
                    TemplateIO.writeTemplate(template, stream);
                }
            } catch (IOException e) {
                BuildingGadgets.LOG.error("Failed to save {}{}!", id, Reference.FILE_ENDING_TEMPLATE, e);
            }
        }

        private void writeTemplateHeaderTo(UUID id, Path templateHeaderFile) {
            try {
                if (! Files.exists(templateHeaderFile))
                    Files.createFile(templateHeaderFile);
                try (OutputStream stream = Files.newOutputStream(templateHeaderFile)) {
                    TemplateIO.writeTemplateHeaderJson(template, stream);
                }
            } catch (IOException e) {
                BuildingGadgets.LOG.error("Failed to save {}{}!", id, Reference.FILE_ENDING_TEMPLATE_HEADER, e);
            }
        }
    }

    private final class ReadTemplateVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            super.visitFile(file, attrs);//ensure file non-null
            if (file.getFileName() == null)
                throw new IOException("Attempted to read from an unnamed file!");
            String name = file.getFileName().toString();
            if (name.endsWith(Reference.FILE_ENDING_TEMPLATE)) {
                String fileName = name.replace(Reference.FILE_ENDING_TEMPLATE, "");
                try {
                    UUID id = UUID.fromString(fileName);
                    Path parent = file.getParent();
                    loadDataForId(id, file, parent != null ? parent.resolve(fileName + Reference.FILE_ENDING_TEMPLATE_HEADER) : null);
                } catch (IllegalArgumentException e) {
                    BuildingGadgets.LOG.warn("Found non id-File {}. This will be ignored for loading Templates.", file);
                } catch (IOException e) {
                    BuildingGadgets.LOG.error("Failed to read Template from {}!", file, e);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
