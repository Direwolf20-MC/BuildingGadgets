package com.direwolf20.buildinggadgets.common.inventory.materials;

import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.PeekingIterator;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;

/* This is currently hidden, to avoid having yet another Registry
 - if it turns out someone needs something else then the default implementations, we can still add that and make it public
 */
interface MaterialListEntry<T extends MaterialListEntry<T>> extends Iterable<ImmutableMultiset<IUniqueObject<?>>> {
    @Override
    PeekingIterator<ImmutableMultiset<IUniqueObject<?>>> iterator();

    Serializer<T> getSerializer();

    MaterialListEntry<?> simplify();

    interface Serializer<T extends MaterialListEntry<T>> extends IForgeRegistryEntry<Serializer<T>> {
        T readFromNBT(CompoundNBT nbt, boolean persisted);

        CompoundNBT writeToNBT(T entry, boolean persisted);

        JsonSerializer<T> asJsonSerializer(boolean printName, boolean extended);
    }
}
