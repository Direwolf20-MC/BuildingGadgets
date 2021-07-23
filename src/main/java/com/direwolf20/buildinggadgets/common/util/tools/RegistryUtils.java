package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

@Tainted(reason = "Oh god no. This shouldn't exist")
public final class RegistryUtils {
    private RegistryUtils() {}

    public static <T extends IForgeRegistryEntry<T>> int getId(IForgeRegistry<T> registry, T value) {
        return ((ForgeRegistry<T>) registry).getID(value);
    }

    public static <T extends IForgeRegistryEntry<T>> T getById(IForgeRegistry<T> registry, int id) {
        return ((ForgeRegistry<T>) registry).getValue(id);
    }

    @Nullable
    public static <T extends IForgeRegistryEntry<T>> T getFromString(IForgeRegistry<T> registry, String resourceLocation) {
        return registry.getValue(new ResourceLocation(resourceLocation));
    }
}
