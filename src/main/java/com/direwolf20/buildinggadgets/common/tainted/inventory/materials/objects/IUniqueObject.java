package com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Some sort of Object that can be interpreted as a RequiredMaterial.
 * Because the Object classes are indexed for performance reasons, this must report the index class and object!
 *
 * @param <T>
 */
public interface IUniqueObject<T> {
    Class<T> getIndexClass();

    T getIndexObject();

    boolean matches(ItemStack stack);

    ItemStack insertInto(ItemStack stack, int count);

    //whether or not this Item prefers to insert via tryCreateInsertStack or via insertInto
    //while insertInto is more generic, it prevents spawning of Items or directly dropping into the player inventory
    //returning false is suggested for all non-Item unique objects, like fluids
    default boolean preferStackInsert() {
        return getIndexClass() == Item.class;
    }

    default Optional<ItemStack> tryCreateInsertStack(Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> index, int count) {
        return Optional.of(createStack(count));
    }

    default ItemStack createStack() {
        return createStack(1);
    }

    //used for writing material-list to a json-String
    default ResourceLocation getObjectRegistryName() {
        T indexObj = getIndexObject();
        if (indexObj instanceof IForgeRegistryEntry)
            return ((IForgeRegistryEntry) indexObj).getRegistryName();
        return new ResourceLocation(BuildingGadgetsAPI.MODID, getIndexClass().getSimpleName());
    }

    ItemStack createStack(int count);

    IUniqueObjectSerializer getSerializer();

}
