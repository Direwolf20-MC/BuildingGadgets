package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public final class Undo {
    static Undo deserialize(CompoundTag nbt) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.WORLD_SAVE_DIM, Tag.TAG_STRING)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST, Tag.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, Tag.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, Tag.TAG_LIST)
                && nbt.contains(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, Tag.TAG_LIST));
        DataDecompressor<ITileDataSerializer> serializerReverseObjectIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getAsString();
                    ITileDataSerializer serializer = RegistryUtils.getFromString(Registries.TILE_DATA_SERIALIZER_REGISTRY.get(), s);
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
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST),
                inbt -> BlockData.deserialize((CompoundTag) inbt, serializerReverseObjectIncrementer, true),
                value -> BlockData.AIR);
        DataDecompressor<IUniqueObjectSerializer> itemSerializerIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST),
                inbt -> {
                    String s = inbt.getAsString();
                    IUniqueObjectSerializer serializer = RegistryUtils.getFromString(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get(), s);
                    if (serializer == null)
                        return SerialisationSupport.uniqueItemSerializer();
                    return serializer;
                },
                value -> {
                    BuildingGadgets.LOG.warn("Attempted to query unknown item-serializer {}. Replacing with default!", value);
                    return SerialisationSupport.uniqueItemSerializer();
                });
        DataDecompressor<Multiset<IUniqueObject<?>>> itemSetReverseObjectIncrementer = new DataDecompressor<>(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST),
                inbt -> NBTHelper.deserializeMultisetEntries((ListTag) inbt, HashMultiset.create(), entry -> readEntry(entry, itemSerializerIncrementer)),
                value -> HashMultiset.create());
        Map<BlockPos, BlockInfo> map = NBTHelper.deserializeMap(
                (ListTag) nbt.get(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST), new HashMap<>(),
                inbt -> NbtUtils.readBlockPos((CompoundTag) inbt),
                inbt -> BlockInfo.deserialize((CompoundTag) inbt, dataReverseObjectIncrementer, itemSetReverseObjectIncrementer));

        ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString(NBTKeys.WORLD_SAVE_DIM)));
        Region bounds = Region.deserializeFrom(nbt.getCompound(NBTKeys.WORLD_SAVE_UNDO_BOUNDS));
        return new Undo(dim, map, bounds);
    }

    private static Tuple<IUniqueObject<?>, Integer> readEntry(Tag inbt, IntFunction<IUniqueObjectSerializer> serializerIntFunction) {
        CompoundTag nbt = (CompoundTag) inbt;
        IUniqueObjectSerializer serializer = serializerIntFunction.apply(nbt.getInt(NBTKeys.UNIQUE_ITEM_SERIALIZER));
        int count = nbt.getInt(NBTKeys.UNIQUE_ITEM_COUNT);
        IUniqueObject<?> item = serializer.deserialize(nbt.getCompound(NBTKeys.UNIQUE_ITEM_ITEM));
        return new Tuple<>(item, count);
    }

    public static Builder builder() {
        return new Builder();
    }

    private ResourceKey<Level> dim;
    private Map<BlockPos, BlockInfo> dataMap;
    private Region boundingBox;

    public Undo(ResourceKey<Level> dim, Map<BlockPos, BlockInfo> dataMap, Region boundingBox) {
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

    CompoundTag serialize() {
        DataCompressor<BlockData> dataObjectIncrementer = new DataCompressor<>();
        DataCompressor<IUniqueObjectSerializer> itemSerializerIncrementer = new DataCompressor<>();
        DataCompressor<Multiset<IUniqueObject<?>>> itemObjectIncrementer = new DataCompressor<>();
        DataCompressor<ITileDataSerializer> serializerObjectIncrementer = new DataCompressor<>();
        CompoundTag res = new CompoundTag();

        ListTag infoList = NBTHelper.serializeMap(dataMap, NbtUtils::writeBlockPos, i -> i.serialize(dataObjectIncrementer, itemObjectIncrementer));
        ListTag dataList = dataObjectIncrementer.write(d -> d.serialize(serializerObjectIncrementer, true));
        ListTag itemSetList = itemObjectIncrementer.write(ms -> NBTHelper.writeIterable(ms.entrySet(), entry -> writeEntry(entry, itemSerializerIncrementer)));
        ListTag dataSerializerList = serializerObjectIncrementer.write(ts -> StringTag.valueOf(Registries.TILE_DATA_SERIALIZER_REGISTRY.get().getKey(ts).toString()));
        ListTag itemSerializerList = itemSerializerIncrementer.write(s -> StringTag.valueOf(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get().getKey(s).toString()));

        res.putString(NBTKeys.WORLD_SAVE_DIM, dim.location().toString());
        res.put(NBTKeys.WORLD_SAVE_UNDO_BLOCK_LIST, infoList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_LIST, dataList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST, dataSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_LIST, itemSetList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST, itemSerializerList);
        res.put(NBTKeys.WORLD_SAVE_UNDO_BOUNDS, boundingBox.serialize());

        return res;
    }

    private CompoundTag writeEntry(Entry<IUniqueObject<?>> entry, ToIntFunction<IUniqueObjectSerializer> serializerObjectIncrementer) {
        CompoundTag res = new CompoundTag();
        res.putInt(NBTKeys.UNIQUE_ITEM_SERIALIZER, serializerObjectIncrementer.applyAsInt(entry.getElement().getSerializer()));
        res.put(NBTKeys.UNIQUE_ITEM_ITEM, entry.getElement().getSerializer().serialize(entry.getElement(), true));
        res.putInt(NBTKeys.UNIQUE_ITEM_COUNT, entry.getCount());
        return res;
    }

    public static final class BlockInfo {
        private static BlockInfo deserialize(CompoundTag nbt, IntFunction<BlockData> dataSupplier, IntFunction<Multiset<IUniqueObject<?>>> itemSetSupplier) {
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

        private CompoundTag serialize(ToIntFunction<BlockData> dataIdSupplier, ToIntFunction<Multiset<IUniqueObject<?>>> itemIdSupplier) {
            CompoundTag res = new CompoundTag();
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

        public Builder record(BlockGetter reader, BlockPos pos, BlockData placeData, Multiset<IUniqueObject<?>> requiredItems, Multiset<IUniqueObject<?>> producedItems) {
            BlockState state = reader.getBlockState(pos);
            BlockEntity te = reader.getBlockEntity(pos);
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

        public Undo build(Level dim) {
            return new Undo(dim.dimension(), mapBuilder.build(), regionBuilder != null ? regionBuilder.build() : Region.singleZero());
        }
    }
}
