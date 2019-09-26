package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.JsonKeys;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.direwolf20.buildinggadgets.api.util.RegistryUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public final class UniqueItem {
    public enum ComparisonMode {
        EXACT_MATCH(0) {
            @Override
            public boolean match(CompoundNBT nbt, @Nullable CompoundNBT other) {
                return nbt.equals(other);
            }
        },
        SUB_TAG_MATCH(1) {
            @Override
            public boolean match(CompoundNBT nbt, @Nullable CompoundNBT other) {
                if (other == null)
                    return false;
                for (String key : nbt.keySet()) {
                    INBT val = nbt.get(key);
                    if (val == null) {
                        if (other.get(key) != null) return false;
                        else continue;
                    }
                    if (! other.contains(key, val.getId()) || other.get(key) == null)
                        return false;
                    if (val.getId() == NBT.TAG_COMPOUND && ! match((CompoundNBT) val, other.getCompound(key)))
                        return false;
                    else if (val.getId() != NBT.TAG_COMPOUND && ! val.getString().equals(other.get(key).getString()))
                        return false;
                }
                return true;
            }
        };
        private static final Byte2ObjectMap<ComparisonMode> BY_ID = new Byte2ObjectOpenHashMap<>();
        private final byte id;

        ComparisonMode(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }

        public abstract boolean match(CompoundNBT nbt, @Nullable CompoundNBT other);

        public static ComparisonMode byId(byte id) {
            ComparisonMode mode = BY_ID.get(id);
            return mode == null ? EXACT_MATCH : mode;//prevent future additions from crashing older clients...
        }

        static {
            Arrays.stream(values()).forEach(m -> BY_ID.put(m.getId(), m));
        }
    }

    public static final Serializer SERIALIZER = new Serializer().setRegistryName(NBTKeys.SIMPLE_UNIQUE_ITEM_ID_RL);

    private final Item item;
    @Nullable
    private final CompoundNBT tagCompound;
    @Nullable
    private final CompoundNBT forgeCaps;
    private final int hash;
    private final ComparisonMode tagMatch;
    private final ComparisonMode capMatch;

    public UniqueItem(Item item) {
        this(item, null, ComparisonMode.EXACT_MATCH);
    }

    public UniqueItem(Item item, @Nullable CompoundNBT tagCompound, ComparisonMode comparisonMode) {
        this(item, tagCompound, comparisonMode, null, ComparisonMode.EXACT_MATCH);
    }

    public UniqueItem(Item item, @Nullable CompoundNBT tagCompound, ComparisonMode tagMatch, @Nullable CompoundNBT forgeCaps, ComparisonMode capMatch) {
        this.item = Objects.requireNonNull(item, "Cannot construct a UniqueItem for a null Item!");
        this.tagCompound = tagCompound;
        this.forgeCaps = forgeCaps;
        this.tagMatch = Objects.requireNonNull(tagMatch);
        this.capMatch = Objects.requireNonNull(capMatch);
        int hash = capMatch.hashCode() + 31 * tagMatch.hashCode();
        hash = tagCompound != null ? tagCompound.hashCode() + 31 * hash : hash;
        hash = forgeCaps != null ? forgeCaps.hashCode() + 31 * hash : hash;
        this.hash = Objects.requireNonNull(item.getRegistryName()).hashCode() + 31 * hash;
    }

    public Item getItem() {
        return item;
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(item, 1);
        if (tagCompound != null) {
            stack.setTag(tagCompound);
        }
        return stack;
    }

    public ResourceLocation getRegistryName() {
        assert item.getRegistryName() != null; //tested in constructor
        return item.getRegistryName();
    }

    @Nullable
    public CompoundNBT getTag() {
        return tagCompound != null ? tagCompound.copy() : null;
    }

    @Nullable
    public CompoundNBT getForgeCaps() {
        return forgeCaps != null ? forgeCaps.copy() : null;
    }

    public ItemStack createStack() {
        return createStack(1);
    }

    public ItemStack createStack(int count) {
        ItemStack res = new ItemStack(item, count, forgeCaps);
        res.setTag(tagCompound);
        return res;
    }

    public boolean matches(ItemStack stack) {
        if (stack.getItem() != getItem())
            return false;
        if (tagCompound != null && ! tagMatch.match(tagCompound, stack.getTag()))
            return false;
        if (forgeCaps != null) {
            //There's no getter for this, so we need to hack or way into it...
            CompoundNBT container = new CompoundNBT();
            stack.write(container);
            CompoundNBT otherCapNBT = container.getCompound("ForgeCaps");
            return capMatch.match(forgeCaps, otherCapNBT);
        }
        return true;
    }

    public Serializer getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof UniqueItem)) return false;

        UniqueItem that = (UniqueItem) o;

        if (! item.equals(that.item)) return false;
        if (tagCompound != null ? ! tagCompound.equals(that.tagCompound) : that.tagCompound != null) return false;
        if (forgeCaps != null ? ! forgeCaps.equals(that.forgeCaps) : that.forgeCaps != null) return false;
        if (tagMatch != that.tagMatch) return false;
        return capMatch == that.capMatch;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("item", getRegistryName())
                .add("tagCompound", tagCompound)
                .add("forgeCaps", forgeCaps)
                .add("tagMatch", tagMatch)
                .add("capMatch", capMatch)
                .toString();
    }

    public static final class Serializer extends ForgeRegistryEntry<Serializer> {
        public CompoundNBT serialize(UniqueItem item, boolean persisted) {
            CompoundNBT res = new CompoundNBT();
            if (item.tagCompound != null)
                res.put(NBTKeys.KEY_DATA, item.tagCompound);
            if (item.forgeCaps != null)
                res.put(NBTKeys.KEY_CAP_NBT, item.forgeCaps);
            if (persisted)
                res.putString(NBTKeys.KEY_ID, item.getRegistryName().toString());
            else
                res.putInt(NBTKeys.KEY_ID, RegistryUtils.getId(ForgeRegistries.ITEMS, item.item));
            res.putByte(NBTKeys.KEY_DATA_COMPARISON, item.tagMatch.getId());
            res.putByte(NBTKeys.KEY_CAP_COMPARISON, item.capMatch.getId());
            return res;
        }

        public UniqueItem deserialize(CompoundNBT res) {
            Preconditions.checkArgument(res.contains(NBTKeys.KEY_ID), "Cannot construct a UniqueItem without an Item!");
            CompoundNBT nbt = res.getCompound(NBTKeys.KEY_DATA);
            ComparisonMode mode = ComparisonMode.byId(res.getByte(NBTKeys.KEY_DATA_COMPARISON));
            CompoundNBT capNbt = res.getCompound(NBTKeys.KEY_CAP_NBT);
            ComparisonMode capMode = ComparisonMode.byId(res.getByte(NBTKeys.KEY_CAP_COMPARISON));
            Item item;
            if (res.contains(NBTKeys.KEY_ID, NBT.TAG_INT))
                item = RegistryUtils.getById(ForgeRegistries.ITEMS, res.getInt(NBTKeys.KEY_ID));
            else
                item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(res.getString(NBTKeys.KEY_ID)));
            return new UniqueItem(item, nbt.isEmpty() ? null : nbt, mode, capNbt.isEmpty() ? null : capNbt, capMode);
        }

        public JsonSerializer<UniqueItem> asJsonSerializer(int count, boolean printName, boolean extended) {
            return (element, typeOfSrc, context) -> {
                JsonObject obj = new JsonObject();
                Item item = element.getItem();
                if (printName)
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NAME, I18n.format(item.getTranslationKey(element.createStack(count))));
                if (extended || ! printName)
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM_ID, context.serialize(element.getRegistryName()));
                if (extended) {
                    if (element.tagCompound != null) {
                        obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NBT, element.tagCompound.toString());
                        obj.add(JsonKeys.MATERIAL_LIST_ITEM_NBT_MATCH, context.serialize(element.tagMatch));
                    }
                    if (element.forgeCaps != null) {
                        obj.addProperty(JsonKeys.MATERIAL_LIST_CAP_NBT, element.forgeCaps.toString());
                        obj.add(JsonKeys.MATERIAL_LIST_CAP_NBT_MATCH, context.serialize(element.capMatch));
                    }
                }
                obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_COUNT, count);
                return obj;
            };
        }
    }
}
