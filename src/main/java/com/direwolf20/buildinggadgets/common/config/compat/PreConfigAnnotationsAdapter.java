package com.direwolf20.buildinggadgets.common.config.compat;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.PatternList;
import net.minecraft.init.Blocks;
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

import static com.direwolf20.buildinggadgets.common.config.CompatConfig.copyValue;
import static com.direwolf20.buildinggadgets.common.config.CompatConfig.setValue;

public class PreConfigAnnotationsAdapter implements IConfigVersionAdapter {
    private static final ComparableVersion MIN_VER = new ComparableVersion("0.0.0");
    private static final Path FILE = Paths.get("BuildingGadgets.cfg");
    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_BLACKLIST = "blockBlacklist";
    private static final String CAT_GADGETS = "Gadgets";
    private static final String CAT_BLACKLIST = "Blacklist Settings";
    private static final String CAT_BUILDING_GADGET = "Building Gadget";
    private static final String CAT_EXCHANGING_GADGET = "Exchanging Gadget";
    private static final String CAT_DESTRUCTION_GADGET = "Destruction Gadget";
    private static final String CAT_COPY_PASTE_GADGET = "Copy-Paste Gadget";

    @Override
    public boolean isApplicableTo(@Nonnull Path modConfigDir, @Nullable ComparableVersion configVersion) {
        return Files.exists(modConfigDir.resolve(FILE));
    }

    @Override
    @Nonnull
    public ConfigCategory updateToVersion(@Nonnull Path modConfigDir, @Nullable ConfigCategory configuration) {
        Path cfgFile = modConfigDir.resolve(FILE);
        Configuration loaded = new Configuration(cfgFile.toFile());
        loaded.load();
        ConfigCategory oldGeneral = loaded.getCategory(CATEGORY_GENERAL);
        ConfigCategory oldBlacklist = loaded.getCategory(CATEGORY_BLACKLIST);
        ConfigCategory res = new ConfigCategory(CATEGORY_GENERAL);
        ConfigCategory gadgets = new ConfigCategory(CAT_GADGETS, res);
        ConfigCategory blacklist = new ConfigCategory(CAT_BLACKLIST, res);
        ConfigCategory catExchanger = new ConfigCategory(CAT_EXCHANGING_GADGET, gadgets);
        ConfigCategory catBuilder = new ConfigCategory(CAT_BUILDING_GADGET, gadgets);
        ConfigCategory catDestruction = new ConfigCategory(CAT_DESTRUCTION_GADGET, gadgets);
        ConfigCategory catCopyPaste = new ConfigCategory(CAT_COPY_PASTE_GADGET, gadgets);

        setValue(res, "Max Build Distance", Integer.toString(32), Type.INTEGER);
        copyValue(res, oldGeneral, "Powered by Forge Energy", "poweredByFE", Boolean.toString(true), Type.BOOLEAN);
        copyValue(res, oldGeneral, "Enable Construction Paste", "enablePaste", Boolean.toString(true), Type.BOOLEAN);
        copyValue(res, oldGeneral, "Enable Destruction Gadget", "enableDestructionTool", Boolean.toString(true), Type.BOOLEAN);
        copyValue(res, oldGeneral, "Default to absolute Coord-Mode", "absoluteCoordinateModeDefault", Boolean.toString(false), Type.BOOLEAN);
        copyValue(res, oldGeneral, "Allow non-Air-Block-Overwrite", "canOverwriteBlocks", Boolean.toString(true), Type.BOOLEAN);

        copyValue(blacklist, oldBlacklist, "Blacklisted Blocks", "Blacklist", new String[]{"minecraft:.*_door.*", PatternList.getName(Blocks.PISTON_HEAD)}, Type.STRING);

        copyValue(gadgets, oldGeneral, "Maximum allowed Range", "maxRange", Integer.toString(15), Type.INTEGER);
        copyValue(gadgets, oldGeneral, "Maximum Energy", "energyMax", Integer.toString(500000), Type.INTEGER);

        copyValue(catBuilder, oldGeneral, "Energy Cost", "energyCostBuilder", Integer.toString(50), Type.INTEGER);
        copyValue(catBuilder, oldGeneral, "Durability", "durabilityBuilder", Integer.toString(500), Type.INTEGER);

        copyValue(catExchanger, oldGeneral, "Energy Cost", "energyCostExchanger", Integer.toString(50), Type.INTEGER);
        copyValue(catExchanger, oldGeneral, "Durability", "durabilityExchanger", Integer.toString(500), Type.INTEGER);

        setValue(catDestruction, "Maximum Energy", Integer.toString(1000000), Type.INTEGER);
        copyValue(catDestruction, oldGeneral, "Energy Cost", "energyCostDestruction", Integer.toString(50), Type.INTEGER);
        copyValue(catDestruction, oldGeneral, "Durability", "durabilityDestruction", Integer.toString(500), Type.INTEGER);

        setValue(catCopyPaste, "Energy Cost", Integer.toString(50), Type.INTEGER);
        copyValue(catCopyPaste, oldGeneral, "Durability", "durabilityCopyPaste", Integer.toString(500), Type.INTEGER);
        try {
            Files.deleteIfExists(cfgFile);
        } catch (IOException e) {
            BuildingGadgets.logger.error("Failed to delete old config File {}.", cfgFile.toString(), e);
        }
        return res;
    }

    @Override
    @Nonnull
    public ComparableVersion getMinVersion() {
        return MIN_VER;
    }
}
