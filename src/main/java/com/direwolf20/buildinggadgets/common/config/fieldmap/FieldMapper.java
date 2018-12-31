package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Class representing a bijective Function and it's reverse Function used to Mapping Field Types to other Types which can be
 * synced more easily.
 * The Value produced by {@link #mapToSync(Object)}will then be given to a {@link ITypeSerializer} to be serialized.
 * The value given to {@link #mapToField(Object)} will be the deserialized Value from an appropriate {@link ITypeSerializer}.
 * @param <FieldVal> The Type of Field this Mapper maps to
 * @param <SyncedVal> The Type of Synced Value this Mapper maps to
 */
public class FieldMapper<FieldVal,SyncedVal> {
    public static final String BLOCK_LIST_MAPPER_ID = "Block List Mapper";

    public static final FieldMapper<Object,Object> GENERIC_IDENTITY_MAPPER = id();
    public static final FieldMapper<ImmutableList<Block>,String[]> BLOCK_LIST_MAPPER = of(
            (list) -> list.stream().map((b) -> Objects.requireNonNull(b.getRegistryName()).toString()).toArray(String[]::new),
            (strings) -> Stream.of(strings).map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).collect(ImmutableList.toImmutableList()));

    private final Function<FieldVal,SyncedVal> fieldToSync;
    private final Function<SyncedVal,FieldVal> syncToField;

    public static <F> FieldMapper<F,F> id() {
        return of(Function.identity(),Function.identity());
    }

    public static <FieldVal,SyncedVal> FieldMapper<FieldVal,SyncedVal> of(Function<FieldVal,SyncedVal> fieldToSync, Function<SyncedVal,FieldVal> syncToField) {
        return new FieldMapper<FieldVal,SyncedVal>(fieldToSync,syncToField){};
    }

    private FieldMapper(Function<FieldVal, SyncedVal> fieldToSync, Function<SyncedVal, FieldVal> syncToField) {
        this.fieldToSync = fieldToSync;
        this.syncToField = syncToField;
    }

    public SyncedVal mapToSync(FieldVal val) {
        return fieldToSync.apply(val);
    }

    public FieldVal mapToField(SyncedVal val) {
        return syncToField.apply(val);
    }
}
