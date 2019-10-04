package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem.Serializer;
import com.direwolf20.buildinggadgets.api.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.util.ObjectIncrementer;
import com.direwolf20.buildinggadgets.api.util.RegistryUtils;
import com.direwolf20.buildinggadgets.api.util.ReverseObjectIncrementer;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public final class Undo {
    static Undo deserialize(CompoundNBT nbt) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.WORLD_SAVE_DIM, NBT.TAG_STRING)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST, NBT.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, NBT.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, NBT.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, NBT.TAG_LIST));
        ReverseObjectIncrementer<ITileDataSerializer> serializerReverseObjectIncrementer = new ReverseObjectIncrementer<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getString();
                    ITileDataSerializer serializer = RegistryUtils.getFromString(Registries.TileEntityData.getTileDataSerializers(), s);
                    if (serializer == null) {
                        BuildingGadgets.LOG.warn("Found unknown serializer {}. Replacing with dummy!", s);
                        serializer = TileSupport.dummyTileEntityData().getSerializer();
                    }
                    return serializer;
                },
                value -> {
                    BuildingGadgetsAPI.LOG.warn("Attempted to query unknown serializer {}. Replacing with dummy!", value);
                    return TileSupport.dummyTileEntityData().getSerializer();
                });
        ReverseObjectIncrementer<BlockData> dataReverseObjectIncrementer = new ReverseObjectIncrementer<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST),
                inbt -> BlockData.deserialize((CompoundNBT) inbt, serializerReverseObjectIncrementer, true),
                value -> BlockData.AIR);
        ReverseObjectIncrementer<UniqueItem.Serializer> itemSerializerIncrementer = new ReverseObjectIncrementer<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getString();
                    UniqueItem.Serializer serializer = UniqueItem.SERIALIZER;
                    if (! s.equals(serializer.getRegistryName().toString()))
                        BuildingGadgets.LOG.warn("Found unknown item-serializer {}. Replacing with default!", s);
                    return serializer;
                },
                value -> {
                    BuildingGadgetsAPI.LOG.warn("Attempted to query unknown item-serializer {}. Replacing with default!", value);
                    return UniqueItem.SERIALIZER;
                });
        ReverseObjectIncrementer<Multiset<UniqueItem>> itemSetReverseObjectIncrementer = new ReverseObjectIncrementer<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST),
                inbt -> NBTHelper.deserializeMultisetEntries((ListNBT) inbt, HashMultiset.create(), entry -> readEntry(entry, itemSerializerIncrementer)),
                value -> HashMultiset.create());
        Map<BlockPos, BlockInfo> map = NBTHelper.deserializeMap(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST), new HashMap<>(),
                inbt -> NBTUtil.readBlockPos((CompoundNBT) inbt),
                inbt -> BlockInfo.deserialize((CompoundNBT) inbt, dataReverseObjectIncrementer, itemSetReverseObjectIncrementer));
        DimensionType dim = DimensionType.byName(new ResourceLocation(nbt.getString(NBTKeys.WORLD_SAVE_DIM)));
        Region bounds = Region.deserializeFrom(nbt.getCompound(NBTKeys.WORLD_SAVE_UNDO_BOUNDS));
        return new Undo(dim, map, bounds);
    }

    private static Pair<UniqueItem, Integer> readEntry(INBT inbt, IntFunction<UniqueItem.Serializer> serializerIntFunction) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        UniqueItem.Serializer serializer = UniqueItem.SERIALIZER;
        Preconditions.checkArgument(serializerIntFunction.apply(nbt.getInt(NBTKeys.UNIQUE_ITEM_SERIALIZER)).equals(serializer));
        int count = nbt.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
        UniqueItem item = serializer.deserialize(nbt.getCompound(NBTKeys.UNIQUE_ITEM_ITEM));
        return Pair.of(item, count);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DimensionType dim;
    private Map<BlockPos, BlockInfo> dataMap;
    private Region boundingBox;

    public Undo(DimensionType dim, Map<BlockPos, BlockInfo> dataMap, Region boundingBox) {
        this.dim = dim;
        this.dataMap = dataMap;
        this.boundingBox = boundingBox;
    }

    public Region getBoundingBox() {
        return boundingBox;
    }

    public Map<BlockPos, BlockInfo> getUndoData() {
        return Collections.unmodifiableMap(dataMap);
    }

    CompoundNBT serialize() {
        ObjectIncrementer<BlockData> dataObjectIncrementer = new ObjectIncrementer<>();
        ObjectIncrementer<UniqueItem.Serializer> itemSerializerIncrementer = new ObjectIncrementer<>();
        ObjectIncrementer<Multiset<UniqueItem>> itemObjectIncrementer = new ObjectIncrementer<>();
        ObjectIncrementer<ITileDataSerializer> serializerObjectIncrementer = new ObjectIncrementer<>();
        CompoundNBT res = new CompoundNBT();
        ListNBT infoList = NBTHelper.serializeMap(dataMap, NBTUtil::writeBlockPos, i -> i.serialize(dataObjectIncrementer, itemObjectIncrementer));
        ListNBT dataSerializerList = serializerObjectIncrementer.write(ts -> new StringNBT(ts.getRegistryName().toString()));
        ListNBT dataList = dataObjectIncrementer.write(d -> d.serialize(serializerObjectIncrementer, true));
        ListNBT itemSerializerList = itemSerializerIncrementer.write(s -> new StringNBT(s.getRegistryName().toString()));
        ListNBT itemSetList = itemObjectIncrementer.write(ms -> NBTHelper.writeIterable(ms.entrySet(), entry -> writeEntry(entry, itemSerializerIncrementer)));
        res.putString(NBTKeys.WORLD_SAVE_DIM, dim.getRegistryName().toString());
        res.put(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, infoList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, dataList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, dataSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST, itemSetList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST, itemSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_BOUNDS, boundingBox.serialize());
        return res;
    }

    private CompoundNBT writeEntry(Entry<UniqueItem> entry, ToIntFunction<Serializer> serializerObjectIncrementer) {
        CompoundNBT res = new CompoundNBT();
        res.putInt(NBTKeys.UNIQUE_ITEM_SERIALIZER, serializerObjectIncrementer.applyAsInt(entry.getElement().getSerializer()));
        res.put(NBTKeys.UNIQUE_ITEM_ITEM, entry.getElement().getSerializer().serialize(entry.getElement(), true));
        res.putInt(NBTKeys.UNIQUE_ITEM_COUNT, entry.getCount());
        return res;
    }

    public static final class BlockInfo {
        private static BlockInfo deserialize(CompoundNBT nbt, IntFunction<BlockData> dataSupplier, IntFunction<Multiset<UniqueItem>> itemSetSupplier) {
            BlockData data = dataSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_DATA));
            Multiset<UniqueItem> usedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED));
            Multiset<UniqueItem> producedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED));
            return new BlockInfo(data, usedItems, producedItems);
        }

        private final BlockData data;
        private final Multiset<UniqueItem> usedItems;
        private final Multiset<UniqueItem> producedItems;

        private BlockInfo(BlockData data, Multiset<UniqueItem> usedItems, Multiset<UniqueItem> producedItems) {
            this.data = data;
            this.usedItems = usedItems;
            this.producedItems = producedItems;
        }

        private CompoundNBT serialize(ToIntFunction<BlockData> dataIdSupplier, ToIntFunction<Multiset<UniqueItem>> itemIdSupplier) {
            CompoundNBT res = new CompoundNBT();
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_DATA, dataIdSupplier.applyAsInt(data));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED, itemIdSupplier.applyAsInt(usedItems));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED, itemIdSupplier.applyAsInt(producedItems));
            return res;
        }

        public BlockData getData() {
            return data;
        }

        public Multiset<UniqueItem> getUsedItems() {
            return Multisets.unmodifiableMultiset(usedItems);
        }

        public Multiset<UniqueItem> getProducedItems() {
            return Multisets.unmodifiableMultiset(producedItems);
        }
    }

    public static final class Builder {
        private final ImmutableMap.Builder<BlockPos, BlockInfo> mapBuilder;
        private Region.Builder regionBuilder;

        private Builder() {
            mapBuilder = ImmutableMap.builder();
            regionBuilder = null;
        }

        public Builder record(IBlockReader reader, BlockPos pos, Multiset<UniqueItem> requiredItems, Multiset<UniqueItem> producedItems) {
            BlockState state = reader.getBlockState(pos);
            TileEntity te = reader.getTileEntity(pos);
            ITileEntityData data = te != null ? NBTTileEntityData.ofTile(te) : TileSupport.dummyTileEntityData();
            return record(pos, new BlockData(state, data), requiredItems, producedItems);
        }

        private Builder record(BlockPos pos, BlockData data, Multiset<UniqueItem> requiredItems, Multiset<UniqueItem> producedItems) {
            mapBuilder.put(pos, new BlockInfo(data, requiredItems, producedItems));
            if (regionBuilder == null)
                regionBuilder = Region.enclosingBuilder();
            regionBuilder.enclose(pos);
            return this;
        }

        public Undo build(DimensionType dim) {
            return new Undo(dim, mapBuilder.build(), regionBuilder != null ? regionBuilder.build() : Region.singleZero());
        }
    }
}
