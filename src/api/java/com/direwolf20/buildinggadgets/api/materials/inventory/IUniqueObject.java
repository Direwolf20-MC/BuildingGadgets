package com.direwolf20.buildinggadgets.api.materials.inventory;

import com.direwolf20.buildinggadgets.api.APIReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUniqueObject<T> {
    Class<T> getIndexClass();

    T getIndexObject();

    boolean matches(ItemStack stack);

    ItemStack insertInto(ItemStack stack, int count);

    default Optional<ItemStack> tryCreateComplexInsertStack(Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> map, int count) {
        return trySimpleInsert(count);
    }

    default Optional<ItemStack> trySimpleInsert(int count) {
        return Optional.of(createStack(count));
    }

    default ItemStack createStack() {
        return createStack(1);
    }

    default ResourceLocation getObjectRegistryName() {
        T indexObj = getIndexObject();
        if (indexObj instanceof IForgeRegistryEntry)
            return ((IForgeRegistryEntry) indexObj).getRegistryName();
        return new ResourceLocation(APIReference.MODID, getIndexClass().getSimpleName());
    }

    ItemStack createStack(int count);

    IUniqueObjectSerializer getSerializer();

}
