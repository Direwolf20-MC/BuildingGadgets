package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.compat.IConfigVersionAdapter;
import com.direwolf20.buildinggadgets.common.config.compat.PreConfigAnnotationsAdapter;
import com.direwolf20.buildinggadgets.common.config.compat.PreConfigVersioningAdapter;
import com.direwolf20.buildinggadgets.common.tools.ReflectionTool;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CompatConfig {
    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_BLACKLIST = "blockBlacklist";
    private static final ImmutableSortedSet<IConfigVersionAdapter> ADAPTERS =
            ImmutableSortedSet
                    .orderedBy(Comparator.comparing(IConfigVersionAdapter::getMinVersion))
                    .add(new PreConfigAnnotationsAdapter(), new PreConfigVersioningAdapter())
                    .build();

    public static void applyCompat(File configDir) {
        Path cfgDir = configDir.toPath();
        ComparableVersion loadedVersion = null;
        Path versionendFile = cfgDir.resolve(BuildingGadgets.MODID + ".cfg");
        if (Files.exists(versionendFile)) { //if the File was present, then the ConfigManager will have loaded the File
            loadedVersion = new ComparableVersion(Config.version);
        }
        ConfigCategory current = null;
        for (IConfigVersionAdapter adapter : ADAPTERS) {
            if (adapter.isApplicableTo(cfgDir, loadedVersion) || current != null) {
                current = adapter.updateToVersion(cfgDir, current);
            }
        }
        Config.version = BuildingGadgets.VERSION;
        if (current != null) {
            parseCompatConfig(current, versionendFile);
        }
        ConfigManager.sync(BuildingGadgets.MODID, Type.INSTANCE);
    }

    private static void parseCompatConfig(ConfigCategory cfg, Path file) {
        /*if (cfg.containsKey("Powered by Forge Energy"))
            Config.poweredByFE = cfg.get("Powered by Forge Energy").getBoolean(true);
        if (cfg.containsKey("Powered by Forge Energy"))
            Config.canOverwriteBlocks = cfg.get("Powered by Forge Energy").getBoolean(true);
        if (cfg.containsKey("Powered by Forge Energy"))
            Config.absoluteCoordDefault = cfg.get("Powered by Forge Energy").getBoolean(true);
        if (cfg.containsKey("Powered by Forge Energy"))
            Config.enableDestructionGadget = cfg.get("Powered by Forge Energy").getBoolean(true);
        if (cfg.containsKey("Powered by Forge Energy"))
            Config.enablePaste = cfg.get("Powered by Forge Energy").getBoolean(true);
        if (cfg.containsKey("Max Build Distance"))
            Config.rayTraceRange = cfg.get("Max Build Distance").getInt(32);
        */
        Configuration saved = new Configuration(file.toFile());
        appendValuesTo(saved, cfg, "");
        saved.save();
        Map<String, Configuration> modConfigs = ReflectionTool.getManagedConfigs();
        if (modConfigs == null) {
            BuildingGadgets.logger.warn("Failed to port Config File, see Log for details!");
            return;
        }
        Configuration forgeLoaded = modConfigs.get(file.toFile().getAbsolutePath());
        if (forgeLoaded == null) {
            BuildingGadgets.logger.warn("Failed to port Config File because Buildinggadgets Config could not be retrieved!");
            return;
        }
        forgeLoaded.load(); //Force forge to reload from disc
    }

    public static Map<String, ConfigCategory> buildCategoryMap(ConfigCategory root) {
        Map<String, ConfigCategory> map = new HashMap<>();
        buildCategoryMap(map, root);
        return map;
    }

    private static void buildCategoryMap(Map<String, ConfigCategory> categoryMap, ConfigCategory root) {
        for (ConfigCategory cat : root.getChildren()) {
            if (!categoryMap.containsKey(cat.getName())) {
                categoryMap.put(cat.getName(), cat);
                buildCategoryMap(categoryMap, cat);
            }
        }
    }

    private static void appendValuesTo(Configuration toAppendAll, ConfigCategory source, String superCats) {
        String thisCat = superCats.isEmpty() ? source.getName() : superCats + Configuration.CATEGORY_SPLITTER + source.getName();
        for (Map.Entry<String, Property> entry : source.entrySet()) {
            Property val = entry.getValue();
            toAppendAll.get(thisCat, entry.getKey(), val.getString(), val.getComment(), val.getType());
        }
        String catAsSuper = thisCat + Configuration.CATEGORY_SPLITTER;
        for (ConfigCategory supCategory : source.getChildren()) {
            appendValuesTo(toAppendAll, supCategory, catAsSuper);
        }
    }

    public static void copyValue(ConfigCategory target, ConfigCategory source, String newKey, String oldKey, String[] defaultValue, Property.Type type) {
        Property prop = source.get(oldKey);
        if (prop != null && prop.getType() == type) {
            if (target == source) return; //no need to copy something that already exists
            setValue(target, newKey, prop.getStringList(), type);
        } else if (prop != null) {
            throw new RuntimeException("Expected Property types to be identical, but " + prop.getType().name() + " cannot be migrated to " + type.name() + "!");
        } else {
            setValue(target, newKey, defaultValue, type);
        }
    }

    public static void copyValue(ConfigCategory target, ConfigCategory source, String newKey, String oldKey, String defaultValue, Property.Type type) {
        Property prop = source.get(oldKey);
        if (prop != null && prop.getType() == type) {
            if (target == source) return; //no need to copy something that already exists
            setValue(target, newKey, prop.getString(), type);
        } else if (prop != null) {
            throw new RuntimeException("Expected Property types to be identical, but " + prop.getType().name() + " cannot be migrated to " + type.name() + "!");
        } else {
            setValue(target, newKey, defaultValue, type);
        }
    }

    public static void setValue(ConfigCategory target, String newKey, String[] defaultValue, Property.Type type) {
        target.put(newKey, new Property(newKey, defaultValue, type));
    }

    public static void setValue(ConfigCategory target, String newKey, String defaultValue, Property.Type type) {
        target.put(newKey, new Property(newKey, defaultValue, type));
    }
}
