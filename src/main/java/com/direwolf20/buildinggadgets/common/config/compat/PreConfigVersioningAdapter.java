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
import java.util.Map;

import static com.direwolf20.buildinggadgets.common.config.CompatConfig.buildCategoryMap;
import static com.direwolf20.buildinggadgets.common.config.CompatConfig.copyValue;

public class PreConfigVersioningAdapter implements IConfigVersionAdapter {
    private static final ComparableVersion MIN_VER = new ComparableVersion("2.6.0");
    private static final Path FILE = Paths.get("Building Gadgets.cfg");
    private static final String CATEGORY_ROOT = "general";
    private static final String CATEGORY_PASTE_CONTAINERS = "Paste Containers";
    private static final String CATEGORY_BUILDER = "Building Gadget";
    private static final String CATEGORY_EXCHANGER = "Exchanging Gadget";
    private static final String CATEGORY_COPY_PASTE = "Copy-Paste Gadget";
    private static final String CATEGORY_DESTRUCTION = "Destruction Gadget";

    @Override
    public boolean isApplicableTo(@Nonnull Path modConfigDir, @Nullable ComparableVersion configVersion) {
        return Files.exists(modConfigDir.resolve(FILE));
    }

    @Override
    @Nonnull
    public ConfigCategory updateToVersion(@Nonnull Path modConfigDir, @Nullable ConfigCategory configuration) {
        ConfigCategory root = getRoot(modConfigDir, configuration);
        Map<String, ConfigCategory> categoryMap = buildCategoryMap(root);
        //the only update required is maybe adding PASTE_CONTAINERS
        updateContainers(root, categoryMap);
        Path cfgFile = modConfigDir.resolve(FILE);
        try {
            Files.deleteIfExists(cfgFile);
        } catch (IOException e) {
            BuildingGadgets.logger.error("Failed to delete old config File {}.", cfgFile.toString(), e);
        }
        return root;//very slight changes only
    }

    /**
     * This is needed, because 2.6.2 added some more categories
     */
    private void updateContainers(ConfigCategory root, Map<String, ConfigCategory> categoryMap) {
        ConfigCategory containers = categoryMap.getOrDefault(CATEGORY_PASTE_CONTAINERS, new ConfigCategory(CATEGORY_PASTE_CONTAINERS, root));
        copyValue(containers, containers, "T1 Container Capacity", "T1 Container Capacity", Integer.toString(512), Type.INTEGER);
        copyValue(containers, containers, "T2 Container Capacity", "T2 Container Capacity", Integer.toString(2048), Type.INTEGER);
        copyValue(containers, containers, "T3 Container Capacity", "T3 Container Capacity", Integer.toString(8192), Type.INTEGER);
    }

    /**
     * This is needed, because 2.6.2 added some more categories
     */
    private void updateGadgets(Map<String, ConfigCategory> categoryMap) {
        ConfigCategory builder = categoryMap.get(CATEGORY_EXCHANGER);
        ConfigCategory exchanger = categoryMap.get(CATEGORY_EXCHANGER);
        ConfigCategory destruction = categoryMap.get(CATEGORY_EXCHANGER);
        ConfigCategory copyPaste = categoryMap.get(CATEGORY_EXCHANGER);
        copyValue(builder, builder, "Damage Cost", "Damage Cost", Integer.toString(1), Type.INTEGER);
        copyValue(exchanger, exchanger, "Damage Cost", "Damage Cost", Integer.toString(2), Type.INTEGER);
        copyValue(destruction, destruction, "Damage Cost", "Damage Cost", Integer.toString(4), Type.INTEGER);
        copyValue(destruction, destruction, "Non-Fuzzy Mode Multiplier", "Non-Fuzzy Mode Multiplier", Double.toString(2), Type.DOUBLE);
        copyValue(destruction, destruction, "Non-Fuzzy Mode Enabled", "Non-Fuzzy Mode Enabled", Boolean.toString(false), Type.BOOLEAN);
        copyValue(copyPaste, copyPaste, "Damage Cost", "Damage Cost", Integer.toString(1), Type.INTEGER);
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
