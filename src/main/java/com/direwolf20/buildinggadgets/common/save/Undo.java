package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

public final class Undo {

    public static Undo deserialize(CompoundNBT nbt) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_SNAPSHOT));
        Multiset<UniqueItem> producedItems =
                NBTHelper.deserializeMultisetEntries((ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED), HashMultiset.create(), Undo::readEntry);
        Multiset<UniqueItem> usedItems =
                NBTHelper.deserializeMultisetEntries((ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED), HashMultiset.create(), Undo::readEntry);
        RegionSnapshot snapshot = RegionSnapshot.deserialize(nbt.getCompound(NBTKeys.WORLD_SAVE_UNDO_SNAPSHOT));
        return new Undo(snapshot, usedItems, producedItems);
    }

    private static Pair<UniqueItem, Integer> readEntry(INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        UniqueItem.Serializer serializer = UniqueItem.SERIALIZER;
        Preconditions.checkArgument(nbt.getString(com.direwolf20.buildinggadgets.api.util.NBTKeys.KEY_SERIALIZER).equals(serializer.getRegistryName().toString()));
        int count = nbt.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
        UniqueItem item = serializer.deserialize(nbt.getCompound(NBTKeys.UNIQUE_ITEM_ITEM));
        return Pair.of(item, count);
    }

    private final RegionSnapshot snapshot;
    private final Multiset<UniqueItem> usedItems;
    private final Multiset<UniqueItem> producedItems; //for the exchanger...

    public Undo(RegionSnapshot snapshot) {
        this(snapshot, ImmutableMultiset.of());
    }

    public Undo(RegionSnapshot snapshot, Multiset<UniqueItem> usedItems) {
        this(snapshot, usedItems, ImmutableMultiset.of());
    }

    public Undo(RegionSnapshot snapshot, Multiset<UniqueItem> usedItems, Multiset<UniqueItem> producedItems) {
        this.snapshot = Objects.requireNonNull(snapshot);
        this.usedItems = Objects.requireNonNull(usedItems);
        this.producedItems = Objects.requireNonNull(producedItems);
    }

    public RegionSnapshot getSnapshot() {
        return snapshot;
    }

    public Multiset<UniqueItem> getUsedItems() {
        return Multisets.unmodifiableMultiset(usedItems);
    }

    public Multiset<UniqueItem> getProducedItems() {
        return Multisets.unmodifiableMultiset(producedItems);
    }

    public CompoundNBT serialize() {
        CompoundNBT res = new CompoundNBT();
        res.put(NBTKeys.WORLD_SAVE_UNDO_SNAPSHOT, snapshot.serialize());
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED, NBTHelper.writeIterable(usedItems.entrySet(), this::writeEntry));
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED, NBTHelper.writeIterable(producedItems.entrySet(), this::writeEntry));
        return res;
    }

    private CompoundNBT writeEntry(Entry<UniqueItem> entry) {
        CompoundNBT res = new CompoundNBT();
        res.putString(com.direwolf20.buildinggadgets.api.util.NBTKeys.KEY_SERIALIZER, entry.getElement().getSerializer().getRegistryName().toString());
        res.put(NBTKeys.UNIQUE_ITEM_ITEM, entry.getElement().getSerializer().serialize(entry.getElement(), true));
        res.putInt(NBTKeys.UNIQUE_ITEM_COUNT, entry.getCount());
        return res;
    }
}
