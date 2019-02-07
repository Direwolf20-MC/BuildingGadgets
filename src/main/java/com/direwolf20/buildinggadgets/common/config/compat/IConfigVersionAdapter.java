package com.direwolf20.buildinggadgets.common.config.compat;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Interface representing Version Adapters, for cases where the Config is changed in an backwards incompatible Manner
 */
public interface IConfigVersionAdapter {
    public default boolean isApplicableTo(@Nonnull Path modConfigDir, @Nullable ComparableVersion configVersion) {
        return configVersion != null && configVersion.compareTo(getMinVersion()) >= 0;
    }

    @Nonnull
    public Configuration updateToVersion(@Nonnull Path modConfigDir, @Nullable Configuration configuration);

    @Nonnull
    public ComparableVersion getMinVersion();
}
