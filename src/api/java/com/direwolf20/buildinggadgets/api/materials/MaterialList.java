package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collection;

public final class MaterialList { //Todo fully implement MaterialList system
    private static final MaterialList EMPTY = new MaterialList();

    public static MaterialList deserialize(CompoundNBT nbt) {
        return MaterialList.deserializeBuilder(nbt).build();
    }

    public static Builder deserializeBuilder(CompoundNBT nbt) {
        Builder builder = builder();
        ListNBT nbtList = nbt.getList(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND);
        for (INBT nbtEntry : nbtList) {
            CompoundNBT compoundEntry = (CompoundNBT) nbtEntry;
            builder.addItem(
                    UniqueItem.deserialize((compoundEntry.getCompound(NBTKeys.KEY_DATA))),
                    compoundEntry.getInt(NBTKeys.KEY_COUNT));
        }
        return builder;
    }

    public static MaterialList empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    private final ImmutableMultiset<UniqueItem> requiredItems;

    private MaterialList() {
        this(ImmutableMultiset.of());
    }

    private MaterialList(Multiset<UniqueItem> requiredItems) {
        this.requiredItems = ImmutableMultiset.copyOf(requiredItems);
    }

    public ImmutableMultiset<UniqueItem> getRequiredItems() {
        return requiredItems;
    }

    public CompoundNBT serialize(boolean persisted) {
        CompoundNBT res = new CompoundNBT();
        ListNBT nbtList = new ListNBT();
        for (Entry<UniqueItem> entry : requiredItems.entrySet()) {
            CompoundNBT nbtEntry = new CompoundNBT();
            nbtEntry.put(NBTKeys.KEY_DATA, entry.getElement().serialize(persisted));
            nbtEntry.putInt(NBTKeys.KEY_COUNT, entry.getCount());
            nbtList.add(nbtEntry);
        }
        res.put(NBTKeys.KEY_DATA, nbtList);
        return res;
    }

    public static final class Builder {
        private Multiset<UniqueItem> requiredItems;

        private Builder() {
            requiredItems = HashMultiset.create();
        }

        public Builder addItem(UniqueItem item, int count) {
            requiredItems.add(item, count);
            return this;
        }

        public Builder addItem(UniqueItem item) {
            return addItem(item, 1);
        }

        public Builder addAll(Collection<UniqueItem> items) {
            requiredItems.addAll(items);
            return this;
        }

        public MaterialList build() {
            return new MaterialList(requiredItems);
        }
    }
}
