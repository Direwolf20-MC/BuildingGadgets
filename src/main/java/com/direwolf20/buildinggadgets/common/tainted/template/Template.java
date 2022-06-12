package com.direwolf20.buildinggadgets.common.tainted.template;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.tainted.building.view.PositionalBuildView;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.direwolf20.buildinggadgets.common.util.compression.DataCompressor;
import com.direwolf20.buildinggadgets.common.util.compression.DataDecompressor;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.MathUtils;
import com.direwolf20.buildinggadgets.common.util.tools.RegistryUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Template {
    public static Template deserialize(CompoundTag nbt, @Nullable TemplateHeader externalHeader, boolean persisted) {
        ListTag posList = nbt.getList(NBTKeys.KEY_POS, Tag.TAG_LONG);
        TemplateHeader.Builder header = TemplateHeader.builderFromNBT(nbt.getCompound(NBTKeys.KEY_HEADER));
        if (externalHeader != null)
            header = header.name(externalHeader.getName()).author(externalHeader.getAuthor());

        DataDecompressor<ITileDataSerializer> serializerDecompressor = persisted ? new DataDecompressor<>(
                nbt.getList(NBTKeys.KEY_SERIALIZER, Tag.TAG_STRING),
                inbt -> RegistryUtils.getFromString(Registries.TileEntityData.getTileDataSerializers(), inbt.getAsString()),
                value -> SerialisationSupport.dummyDataSerializer())
                : null;

        DataDecompressor<BlockData> dataDecompressor = new DataDecompressor<>(
                nbt.getList(NBTKeys.KEY_DATA, Tag.TAG_COMPOUND),
                inbt -> persisted ?
                        BlockData.tryDeserialize((CompoundTag) inbt, serializerDecompressor, true) :
                        BlockData.tryDeserialize((CompoundTag) inbt, false),
                value -> BlockData.AIR);

        ImmutableMap.Builder<BlockPos, BlockData> mapBuilder = ImmutableMap.builder();
        for (Tag inbt : posList) {
            LongTag longNBT = (LongTag) inbt;
            BlockPos pos = MathUtils.posFromLong(longNBT.getAsLong());
            BlockData data = dataDecompressor.apply(MathUtils.readStateId(longNBT.getAsLong()));
            mapBuilder.put(pos, data);
        }
        return new Template(mapBuilder.build(), header.build());
    }

    private final ImmutableMap<BlockPos, BlockData> map;
    private TemplateHeader header; //the only modification, this may ever receive, is evaluating the requiredItems!
    private boolean isNormalized;

    public Template(ImmutableMap<BlockPos, BlockData> map, TemplateHeader header) {
        this(map, header, false);
    }

    private Template(ImmutableMap<BlockPos, BlockData> map, TemplateHeader header, boolean isNormalized) {
        this.map = map;
        this.header = header;
        this.isNormalized = isNormalized;
    }

    public Template() {
        this(ImmutableMap.of(), TemplateHeader.builder(Region.singleZero()).build());
    }

    public TemplateHeader getHeaderAndForceMaterials(BuildContext context) {
        if (header.getRequiredItems() == null) {
            MaterialList materialList = CommonUtils.estimateRequiredItems(
                    createViewInContext(context),
                    context,
                    context.getPlayer() != null ?
                            context.getPlayer().position().add(0, context.getPlayer().getEyeHeight(), 0) :
                            null);
            header = TemplateHeader.builderOf(header).requiredItems(materialList).build();
        }
        return getHeader();
    }

    public TemplateHeader getHeader() {
        return header;
    }

    public IBuildView createViewInContext(BuildContext context) {
        return PositionalBuildView.createUnsafe(context, map, header.getBoundingBox());
    }

    public CompoundTag serialize(boolean persisted) {
        if (!isNormalized)
            return normalize().serialize(persisted);
        CompoundTag res = new CompoundTag();
        ListTag posList = new ListTag();
        DataCompressor<BlockData> blockDataCompressor = new DataCompressor<>();
        DataCompressor<ITileDataSerializer> dataSerializerCompressor = new DataCompressor<>();
        for (Map.Entry<BlockPos, BlockData> entry : map.entrySet()) {
            long posEntry = MathUtils.includeStateId(MathUtils.posToLong(entry.getKey()), blockDataCompressor.applyAsInt(entry.getValue()));
            posList.add(LongTag.valueOf(posEntry));
        }
        ListTag dataList = blockDataCompressor.write(d -> persisted ?
                d.serialize(dataSerializerCompressor, true)
                : d.serialize(false));
        ListTag serializerList = persisted ? dataSerializerCompressor.write(s -> StringTag.valueOf(Registries.TILE_DATA_SERIALIZER_REGISTRY.get().getKey(s).toString())) : null;
        res.put(NBTKeys.KEY_DATA, dataList);
        res.put(NBTKeys.KEY_POS, posList);
        res.put(NBTKeys.KEY_HEADER, header.toNBT(persisted));
        if (persisted)
            res.put(NBTKeys.KEY_SERIALIZER, serializerList);
        return res;
    }

    public Template rotate(Rotation rotation) {
        return rotate(Axis.Y, rotation);
    }

    public Template rotate(Axis axis, Rotation rotation) {
        if (map.isEmpty()) //saves some time and prevents problems with enclosing builder
            return this;
        int[][] matrix = MathUtils.rotationMatrixFor(axis, rotation);
        rotation = axis == Axis.Y ? rotation : Rotation.NONE; //BlockState's can only rotate around the y-Axis
        ImmutableMap.Builder<BlockPos, BlockData> mapBuilder = ImmutableMap.builder();
        Region.Builder regionBuilder = Region.enclosingBuilder();
        for (Map.Entry<BlockPos, BlockData> entry : map.entrySet()) {
            BlockPos newPos = MathUtils.matrixMul(matrix, entry.getKey());
            mapBuilder.put(newPos, entry.getValue().rotate(rotation));
            regionBuilder.enclose(newPos);
        }
        return new Template(mapBuilder.build(), TemplateHeader.builderOf(header, regionBuilder.build()).build()).normalize();
    }

    public Template mirror(Axis axis) {
        int xFac = 1;
        int zFac = 1;
        Mirror mirror;
        switch (axis) {
            case X:
                mirror = Mirror.LEFT_RIGHT;
                zFac = -1;
                break;
            case Z:
                mirror = Mirror.FRONT_BACK;
                xFac = -1;
                break;
            default:
                mirror = Mirror.NONE;
        }
        Region.Builder regionBuilder = Region.enclosingBuilder();
        ImmutableMap.Builder<BlockPos, BlockData> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<BlockPos, BlockData> entry : map.entrySet()) {
            BlockPos newPos = new BlockPos(entry.getKey().getX() * xFac, entry.getKey().getY(), entry.getKey().getZ() * zFac);
            mapBuilder.put(newPos, entry.getValue().mirror(mirror));
            regionBuilder.enclose(newPos);
        }
        return new Template(mapBuilder.build(), TemplateHeader.builderOf(header, regionBuilder.build()).build()).normalize();
    }

    public Template replace(Function<BlockPos, Optional<BlockData>> replacements) {
        ImmutableMap.Builder<BlockPos, BlockData> mapBuilder = ImmutableMap.builder();
        for (Map.Entry<BlockPos, BlockData> entry : map.entrySet()) {
            mapBuilder.put(entry.getKey(), replacements.apply(entry.getKey()).orElse(entry.getValue()));
        }
        return new Template(mapBuilder.build(), header, isNormalized);
    }

    public Template withName(@Nullable String name) {
        return new Template(map, TemplateHeader.builderOf(header).name(name).build());
    }

    public Template withNameAndAuthor(@Nullable String name, @Nullable String author) {
        return new Template(map, TemplateHeader.builderOf(header).name(name).author(author).build());
    }

    public Template clearMaterials() {
        return new Template(map, TemplateHeader.builderOf(header).requiredItems(null).build());
    }

    public Template normalize() {
        if (isNormalized)
            return this;
        Region region = header.getBoundingBox();
        ImmutableMap.Builder<BlockPos, BlockData> builder = ImmutableMap.builder();
        for (Map.Entry<BlockPos, BlockData> entry : map.entrySet()) {
            builder.put(entry.getKey().subtract(region.getMin()), entry.getValue());
        }
        return new Template(builder.build(), TemplateHeader.builderOf(header, region.inverseTranslate(region.getMin())).build(), true);
    }
}