package com.direwolf20.buildinggadgets.common.util.tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;

public final class RegistryUtils {
    private RegistryUtils() {
    }

    public static <T> int getId(IForgeRegistry<T> registry, T value) {
        return ((ForgeRegistry<T>) registry).getID(value);
    }

    public static <T> T getById(IForgeRegistry<T> registry, int id) {
        return ((ForgeRegistry<T>) registry).getValue(id);
    }

    @Nullable
    public static <T> T getFromString(IForgeRegistry<T> registry, String resourceLocation) {
        return registry.getValue(new ResourceLocation(resourceLocation));
    }

    public static <T> ResourceLocation getIdFromRegistry(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }

    public static ResourceLocation getItemId(Item item) {
        return getIdFromRegistry(ForgeRegistries.ITEMS, item);
    }

    public static ResourceLocation getBlockId(Block item) {
        return getIdFromRegistry(ForgeRegistries.BLOCKS, item);
    }
}
