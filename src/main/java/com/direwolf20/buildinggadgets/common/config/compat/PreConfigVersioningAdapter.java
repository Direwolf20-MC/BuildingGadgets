package com.direwolf20.buildinggadgets.common.config.compat;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PreConfigVersioningAdapter implements IConfigVersionAdapter {
    private static final ComparableVersion MIN_VER = new ComparableVersion("0.0.0");

    @Override
    public boolean isApplicableTo(@Nonnull Path modConfigDir, @Nullable ComparableVersion configVersion) {
        return configVersion == null && Files.exists(modConfigDir.resolve(Paths.get("Building Gadgets.cfg")));
    }

    @Override
    @Nonnull
    public Configuration updateToVersion(@Nonnull Path modConfigDir, @Nullable Configuration configuration) {
        return new Configuration();
    }

    @Override
    @Nonnull
    public ComparableVersion getMinVersion() {
        return MIN_VER;
    }
}
