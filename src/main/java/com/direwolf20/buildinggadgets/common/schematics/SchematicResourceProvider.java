package com.direwolf20.buildinggadgets.common.schematics;

import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SchematicResourceProvider {
    public static SchematicResourceProvider createInstance(File modConfigDir) {
        return new SchematicResourceProvider(modConfigDir.toPath());
    }

    private static final String SCHEMATIC_DIR = "schematics/";
    private Map<ResourceLocation, ISchematic> schematics;
    private Path schematicSourceDir;

    private SchematicResourceProvider(Path modConfigDir) {
        schematics = new HashMap<>();
        schematicSourceDir = modConfigDir.resolve(SCHEMATIC_DIR);
    }

    public void reload() {

    }
}
