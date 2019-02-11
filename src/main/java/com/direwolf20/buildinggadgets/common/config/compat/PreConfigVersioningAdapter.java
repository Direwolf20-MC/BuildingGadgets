package com.direwolf20.buildinggadgets.common.config.compat;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property.Type;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.direwolf20.buildinggadgets.common.config.CompatConfig.setValue;

public class PreConfigVersioningAdapter implements IConfigVersionAdapter {
    private static final ComparableVersion MIN_VER = new ComparableVersion("2.6.0");
    private static final Path FILE = Paths.get("Building Gadgets.cfg");
    private static final String CATEGORY_ROOT = "general";
    private static final String CATEGORY_PASTE_CONTAINERS = "Paste Containers";

    @Override
    public boolean isApplicableTo(@Nonnull Path modConfigDir, @Nullable ComparableVersion configVersion) {
        return configVersion == null && Files.exists(modConfigDir.resolve(FILE));
    }

    @Override
    @Nonnull
    public ConfigCategory updateToVersion(@Nonnull Path modConfigDir, @Nullable ConfigCategory configuration) {
        ConfigCategory root = getRoot(modConfigDir, configuration);
        ConfigCategory containers = null;
        //the only update required is maybe adding PASTE_CONTAINERS
        for (ConfigCategory cat : root.getChildren()) {
            if (cat.getName().equals(CATEGORY_PASTE_CONTAINERS)) {
                containers = cat;
                break;
            }
        }
        if (containers == null) {
            containers = new ConfigCategory(CATEGORY_PASTE_CONTAINERS);
            setValue(containers, "T1 Container Capacity", Integer.toString(512), Type.INTEGER);
            setValue(containers, "T2 Container Capacity", Integer.toString(2048), Type.INTEGER);
            setValue(containers, "T3 Container Capacity", Integer.toString(8192), Type.INTEGER);
        }
        Path cfgFile = modConfigDir.resolve(FILE);
        try {
            Files.deleteIfExists(cfgFile);
        } catch (IOException e) {
            BuildingGadgets.logger.error("Failed to delete old config File {}.", cfgFile.toString(), e);
        }
        return root;//very slight changes only
    }

    @Override
    @Nonnull
    public ComparableVersion getMinVersion() {
        return MIN_VER;
    }

    private ConfigCategory getRoot(@Nonnull Path modConfigDir, @Nullable ConfigCategory configuration) {
        if (configuration != null) return configuration;
        Configuration cfg = new Configuration(modConfigDir.resolve(FILE).toFile());
        cfg.load();
        return cfg.getCategory(CATEGORY_ROOT);
    }
}
