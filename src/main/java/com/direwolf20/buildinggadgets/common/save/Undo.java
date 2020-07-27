package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.registry.Registries;
import com.direwolf20.buildinggadgets.common.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.compression.DataCompressor;
import com.direwolf20.buildinggadgets.common.util.compression.DataDecompressor;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.RegistryUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

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
        DataDecompressor<ITileDataSerializer> serializerReverseObjectIncrementer = new DataDecompressor<>(
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
                    BuildingGadgets.LOG.warn("Attempted to query unknown serializer {}. Replacing with dummy!", value);
                    return TileSupport.dummyTileEntityData().getSerializer();
                });
        DataDecompressor<BlockData> dataReverseObjectIncrementer = new DataDecompressor<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST),
                inbt -> BlockData.deserialize((CompoundNBT) inbt, serializerReverseObjectIncrementer, true),
                value -> BlockData.AIR);
        DataDecompressor<IUniqueObjectSerializer> itemSerializerIncrementer = new DataDecompressor<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getString();
                    IUniqueObjectSerializer serializer = RegistryUtils.getFromString(Registries.getUniqueObjectSerializers(), s);
                    if (serializer == null)
                        return SerialisationSupport.uniqueItemSerializer();
                    return serializer;
                },
                value -> {
                    BuildingGadgets.LOG.warn("Attempted to query unknown item-serializer {}. Replacing with default!", value);
                    return SerialisationSupport.uniqueItemSerializer();
                });
        DataDecompressor<Multiset<IUniqueObject<?>>> itemSetReverseObjectIncrementer = new DataDecompressor<>(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST),
                inbt -> NBTHelper.deserializeMultisetEntries((ListNBT) inbt, HashMultiset.create(), entry -> readEntry(entry, itemSerializerIncrementer)),
                value -> HashMultiset.create());
        Map<BlockPos, BlockInfo> map = NBTHelper.deserializeMap(
                (ListNBT) nbt.get(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST), new HashMap<>(),
                inbt -> NBTUtil.readBlockPos((CompoundNBT) inbt),
                inbt -> BlockInfo.deserialize((CompoundNBT) inbt, dataReverseObjectIncrementer, itemSetReverseObjectIncrementer));

        RegistryKey<DimensionType> dim = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new ResourceLocation(nbt.getString(NBTKeys.WORLD_SAVE_DIM)));
        Region bounds = Region.deserializeFrom(nbt.getCompound(NBTKeys.WORLD_SAVE_UNDO_BOUNDS));
        return new Undo(dim, map, bounds);
    }

    private static Tuple<IUniqueObject<?>, Integer> readEntry(INBT inbt, IntFunction<IUniqueObjectSerializer> serializerIntFunction) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        IUniqueObjectSerializer serializer = serializerIntFunction.apply(nbt.getInt(NBTKeys.UNIQUE_ITEM_SERIALIZER));
        int count = nbt.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
        IUniqueObject<?> item = serializer.deserialize(nbt.getCompound(NBTKeys.UNIQUE_ITEM_ITEM));
        return new Tuple<>(item, count);
    }

    public static Builder builder() {
        return new Builder();
    }

    private RegistryKey<DimensionType> dim;
    private Map<BlockPos, BlockInfo> dataMap;
    private Region boundingBox;

    public Undo(RegistryKey<DimensionType> dim, Map<BlockPos, BlockInfo> dataMap, Region boundingBox) {
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
        DataCompressor<BlockData> dataObjectIncrementer = new DataCompressor<>();
        DataCompressor<IUniqueObjectSerializer> itemSerializerIncrementer = new DataCompressor<>();
        DataCompressor<Multiset<IUniqueObject<?>>> itemObjectIncrementer = new DataCompressor<>();
        DataCompressor<ITileDataSerializer> serializerObjectIncrementer = new DataCompressor<>();
        CompoundNBT res = new CompoundNBT();

        ListNBT infoList = NBTHelper.serializeMap(dataMap, NBTUtil::writeBlockPos, i -> i.serialize(dataObjectIncrementer, itemObjectIncrementer));
        ListNBT dataList = dataObjectIncrementer.write(d -> d.serialize(serializerObjectIncrementer, true));
        ListNBT itemSetList = itemObjectIncrementer.write(ms -> NBTHelper.writeIterable(ms.entrySet(), entry -> writeEntry(entry, itemSerializerIncrementer)));
        ListNBT dataSerializerList = serializerObjectIncrementer.write(ts -> StringNBT.of(ts.getRegistryName().toString()));
        ListNBT itemSerializerList = itemSerializerIncrementer.write(s -> StringNBT.of(s.getRegistryName().toString()));

        res.putString(NBTKeys.WORLD_SAVE_DIM, dim.getRegistryName().toString());
        res.put(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, infoList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, dataList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, dataSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST, itemSetList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST, itemSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_BOUNDS, boundingBox.serialize());

        return res;
    }

    private CompoundNBT writeEntry(Entry<IUniqueObject<?>> entry, ToIntFunction<IUniqueObjectSerializer> serializerObjectIncrementer) {
        CompoundNBT res = new CompoundNBT();
        res.putInt(NBTKeys.UNIQUE_ITEM_SERIALIZER, serializerObjectIncrementer.applyAsInt(entry.getElement().getSerializer()));
        res.put(NBTKeys.UNIQUE_ITEM_ITEM, entry.getElement().getSerializer().serialize(entry.getElement(), true));
        res.putInt(NBTKeys.UNIQUE_ITEM_COUNT, entry.getCount());
        return res;
    }

    public static final class BlockInfo {
        private static BlockInfo deserialize(CompoundNBT nbt, IntFunction<BlockData> dataSupplier, IntFunction<Multiset<IUniqueObject<?>>> itemSetSupplier) {
            BlockData data = dataSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_RECORDED_DATA));
            BlockData placedData = dataSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_PLACED_DATA));
            Multiset<IUniqueObject<?>> usedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED));
            Multiset<IUniqueObject<?>> producedItems = itemSetSupplier.apply(nbt.getInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED));
            return new BlockInfo(data, placedData, usedItems, producedItems);
        }

        private final BlockData recordedData;
        private final BlockData placedData;
        private final Multiset<IUniqueObject<?>> usedItems;
        private final Multiset<IUniqueObject<?>> producedItems;

        private BlockInfo(BlockData recordedData, BlockData placedData, Multiset<IUniqueObject<?>> usedItems, Multiset<IUniqueObject<?>> producedItems) {
            this.recordedData = recordedData;
            this.placedData = placedData;
            this.usedItems = usedItems;
            this.producedItems = producedItems;
        }

        private CompoundNBT serialize(ToIntFunction<BlockData> dataIdSupplier, ToIntFunction<Multiset<IUniqueObject<?>>> itemIdSupplier) {
            CompoundNBT res = new CompoundNBT();
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_RECORDED_DATA, dataIdSupplier.applyAsInt(recordedData));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_PLACED_DATA, dataIdSupplier.applyAsInt(placedData));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_USED, itemIdSupplier.applyAsInt(usedItems));
            res.putInt(NBTKeys.WORLD_SAVE_UNDO_ITEMS_PRODUCED, itemIdSupplier.applyAsInt(producedItems));
            return res;
        }

        public BlockData getRecordedData() {
            return recordedData;
        }

        public BlockData getPlacedData() {
            return placedData;
        }

        public Multiset<IUniqueObject<?>> getUsedItems() {
            return Multisets.unmodifiableMultiset(usedItems);
        }

        public Multiset<IUniqueObject<?>> getProducedItems() {
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

        public Builder record(IBlockReader reader, BlockPos pos, BlockData placeData, Multiset<IUniqueObject<?>> requiredItems, Multiset<IUniqueObject<?>> producedItems) {
            BlockState state = reader.getBlockState(pos);
            TileEntity te = reader.getTileEntity(pos);
            ITileEntityData data = te != null ? NBTTileEntityData.ofTile(te) : TileSupport.dummyTileEntityData();
            return record(pos, new BlockData(state, data), placeData, requiredItems, producedItems);
        }

        private Builder record(BlockPos pos, BlockData recordedData, BlockData placedData, Multiset<IUniqueObject<?>> requiredItems, Multiset<IUniqueObject<?>> producedItems) {
            mapBuilder.put(pos, new BlockInfo(recordedData, placedData, requiredItems, producedItems));
            if (regionBuilder == null)
                regionBuilder = Region.enclosingBuilder();
            regionBuilder.enclose(pos);
            return this;
        }

        public Undo build(World dim) {
            return new Undo(dim.getDimensionRegistryKey(), mapBuilder.build(), regionBuilder != null ? regionBuilder.build() : Region.singleZero());
        }
    }
}
