package com.direwolf20.buildinggadgets.common.schema.template;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.schema.BoundingBox;
import com.direwolf20.buildinggadgets.common.schema.template.TemplateData.BlockData;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import jdk.nashorn.internal.ir.annotations.Immutable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Immutable
public final class Template implements Iterable<TemplateData> {
    private static final Collector<INBT, ListNBT, ListNBT> LIST_COLLECTOR =
            Collector.of(ListNBT::new, ListNBT::add, (l1, l2) -> {l1.addAll(l2); return l1;});
    private static final String KEY_STATE_POS = "state_pos";
    private static final String KEY_STATE_DATA = "state_data";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TILE_POS = "tile_pos";
    private static final String KEY_TILE_DATA = "tile_data";
    private static final String LOG_STATES = "states";
    private static final String LOG_TILES = "tiles";
    private static final int BYTE_MASK_12BIT = 0xF_FF;
    private static final int BYTE_MASK_14BIT = 0x3F_FF;
    private static final int BYTE_MASK_24BIT = 0xFF_FF_FF;
    private static final long BYTE_MASK_40BIT = ((long) 0xFF_FF_FF_FF) << 8 | 0xFF;
    private static final int MAX_NUMBER_OF_IDS = (1 << 24) - 1;
    private static final Comparator<BlockPos> YXZ_COMPARATOR =
            Comparator.comparing(BlockPos::getY).thenComparing(BlockPos::getX).thenComparing(BlockPos::getZ);
    private static final Comparator<TemplateData> BY_POS_YXZ = Comparator.comparing(TemplateData::getPos, YXZ_COMPARATOR);
    private static final int[][] IDENTITY_3X3 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    //sorted to be able to guarantee iteration order
    private final ImmutableSortedSet<TemplateData> data;
    private final BoundingBox bounds;
    private final String author;

    private Template(ImmutableSortedSet<TemplateData> data, BoundingBox bounds, String author) {
        this.data = data;
        this.bounds = bounds;
        this.author = author;
    }

    public static Optional<Template> deserializeNBT(CompoundNBT nbt) {
        if (! nbt.contains(KEY_STATE_POS, NBT.TAG_LONG_ARRAY) || ! nbt.contains(KEY_STATE_DATA, NBT.TAG_LIST))
            return Optional.empty();

        //read the states
        ListNBT nbtData = (ListNBT) nbt.get(KEY_STATE_DATA);
        assert nbtData != null;
        List<BlockState> uniqueStates = new ArrayList<>(nbtData.size());
        for (INBT dataNBT : nbtData) {
            //read blockstate will replace everything unknown with the default value for Blocks: AIR
            BlockState state = NBTUtil.readBlockState((CompoundNBT) dataNBT);
            //a trace log, as this may be normal... In the future we might want to replace this with some placeholder
            if (state == Blocks.AIR.getDefaultState())
                BuildingGadgets.LOGGER.trace("Found unknown state with nbt {}. Will be ignored!", dataNBT);
            uniqueStates.add(state);
        }
        List<CompoundNBT> uniqueTiles = nbt.contains(KEY_TILE_DATA, NBT.TAG_LIST) ?
                ((ListNBT) nbt.get(KEY_TILE_DATA)).stream().map(n -> (CompoundNBT) n).collect(ImmutableList.toImmutableList()) :
                ImmutableList.of();

        ImmutableSortedSet.Builder<TemplateData> builder = ImmutableSortedSet.orderedBy(BY_POS_YXZ);
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        long[] tilePos = nbt.getLongArray(KEY_TILE_POS); //returns empty arry if not present
        int tileIndex = 0;
        for (long packed : nbt.getLongArray(KEY_STATE_POS)) {
            int id = getId(packed);
            long posLong = getPosLong(packed);
            BlockPos thePos = posFromLong(posLong);
            if (id >= uniqueStates.size() || id < 0) {
                BuildingGadgets.LOGGER.error("Found illegal state-id {} in Template at pos {}(={}), when {} states " +
                                "are known. The Template is therefore deemed broken.",
                        id, posLong, thePos, uniqueStates.size());
                return Optional.empty();
            }

            BlockState state = uniqueStates.get(id);
            //Skip unknown Blocks - this has no log, as it is already logged before and even might happen more
            //than once. So having a log here might result in serious log spam
            if (state == Blocks.AIR.getDefaultState())
                continue;
            //I'd actually like to outsource this to a class, but Mike told me not to... (see Builder#recordBlock)
            minX = Math.min(thePos.getX(), minX);
            minY = Math.min(thePos.getY(), minY);
            minZ = Math.min(thePos.getZ(), minZ);
            maxX = Math.max(thePos.getX(), maxX);
            maxY = Math.max(thePos.getY(), maxY);
            maxZ = Math.max(thePos.getZ(), maxZ);

            CompoundNBT tileData = null;
            if (tileIndex < tilePos.length && tileIndex >= 0) {
                long tilePosLong = getPosLong(tilePos[tileIndex]);
                if (tilePosLong == posLong) {
                    int tileId = getId(tilePos[tileIndex++]);
                    if (tileId < uniqueTiles.size() && tileId >= 0)
                        tileData = uniqueTiles.get(tileId);
                    else
                        BuildingGadgets.LOGGER.error("Found illegal tile-id {} in Template at pos {}(={}), " +
                                        "when {} tiles are known. Skipping tile data.",
                                tileId, tilePos, thePos, uniqueTiles.size());
                }
            }

            builder.add(new TemplateData(thePos, new BlockData(state, tileData)));
        }
        //re-evaluate the bounding box based on potentially missing blocks
        BoundingBox bounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        String author = nbt.getString(KEY_AUTHOR);
        //The Template may no longer have 0,0,0 min - if the corresponding blocks had to be removed
        //Therefore normalize the result.
        return Optional.of(new Template(builder.build(), bounds, author))
                .map(Template::normalize);
    }

    /**
     * Create a new Builder with the specified translation. All Blocks recorded by the {@link Builder} must have positions
     * such that for any pos a, {@code a.subtract(translationPos)} returns a pos with only positive coordinates.
     * <p>
     * This can for example be achieved by using {@link BoundingBox#createMinPos()} to get the translationPos and then
     * record only positions within the {@link BoundingBox}.
     *
     * @param translationPos The origin of the resulting {@code Template}'s coordinate system, expressed in the world
     *                       coordinate system.
     * @return A new {@link Builder}
     * @see Builder#recordBlock(BlockPos, BlockState, CompoundNBT)
     */
    public static Builder builder(BlockPos translationPos) {
        return new Builder(translationPos);
    }

    /**
     * Converts a {@link Vec3i} into a long representation. Notice that only the first 40bits are used, because the
     * remaining 24bit are reserved for state-id's. This is also why {@link BlockPos#toLong()} cannot be used.
     *
     * @param vec The vector to "longify"
     * @return A long consisting of [24bit-unused]|[14bit-x_coord]|[12bit-y_coord]|[14bit-z_coord]
     */
    private static long vecToLong(Vec3i vec) {
        long res = (long) (vec.getX() & BYTE_MASK_14BIT) << 26;
        res |= (vec.getY() & BYTE_MASK_12BIT) << 14;
        res |= (vec.getZ() & BYTE_MASK_14BIT);
        return res;
    }

    private static long includeId(long posLong, int stateId) {
        return posLong | ((long) (stateId & BYTE_MASK_24BIT) << 40);
    }

    private static int getId(long packed) {
        return (int) ((packed >> 40) & BYTE_MASK_24BIT);
    }

    private static long getPosLong(long packed) {
        return packed & BYTE_MASK_40BIT;
    }

    /**
     * Converts longs provided by {@link #vecToLong(Vec3i)} back into a {@link BlockPos}.
     *
     * @param serialized the long produced by {@link #vecToLong(Vec3i)}
     * @return A new position from the serialized long.
     */
    private static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 26) & BYTE_MASK_14BIT);
        int y = (int) ((serialized >> 14) & BYTE_MASK_12BIT);
        int z = (int) (serialized & BYTE_MASK_14BIT);
        return new BlockPos(x, y, z);
    }

    private static int sineForRotation(Rotation rot) {
        switch (rot) {
            case NONE:
            case CLOCKWISE_180:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case COUNTERCLOCKWISE_90:
                return - 1;
            default:
                throw new AssertionError("Unknown Rotation " + rot);
        }
    }

    private static int cosineForRotation(Rotation rot) {
        return sineForRotation(rot.add(Rotation.CLOCKWISE_90));
    }

    private static int[][] rotationMatrixFor(Axis axis, Rotation rotation) { //copied from #495
        int[][] matrix = new int[3][3]; //remember it's Java => everything initiated to 0
        switch (axis) {
            case X: {
                matrix[0][0] = 1;
                matrix[1][1] = cosineForRotation(rotation);
                matrix[1][2] = sineForRotation(rotation);
                matrix[2][1] = - sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Y: {
                matrix[1][1] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[2][0] = sineForRotation(rotation);
                matrix[0][2] = - sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Z: {
                matrix[2][2] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[0][1] = sineForRotation(rotation);
                matrix[1][0] = - sineForRotation(rotation);
                matrix[1][1] = cosineForRotation(rotation);
                break;
            }
        }
        return matrix;
    }

    private static int[][] mirrorMatrixFor(Axis axis) {
        switch (axis) {
            case X:
                return new int[][]{{- 1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
            case Y:
                return new int[][]{{1, 0, 0}, {0, - 1, 0}, {0, 0, 1}};
            case Z:
                return new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, - 1}};
            default:
                throw new AssertionError("Unknown Axis " + axis);
        }
    }

    private static BlockPos matrixMul(int[][] matrix, Vec3i invTranslation, BlockPos pos) {
        assert matrix.length == 3 && matrix[0].length == 3 && matrix[1].length == 3 && matrix[2].length == 3;
        int x = pos.getX() * matrix[0][0] + pos.getY() * matrix[0][1] + pos.getZ() * matrix[0][2] - invTranslation.getX();
        int y = pos.getX() * matrix[1][0] + pos.getY() * matrix[1][1] + pos.getZ() * matrix[1][2] - invTranslation.getY();
        int z = pos.getX() * matrix[2][0] + pos.getY() * matrix[2][1] + pos.getZ() * matrix[2][2] - invTranslation.getZ();
        return new BlockPos(x, y, z);
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getAuthor() {
        return author;
    }

    public int size() {
        return data.size();
    }

    public CompoundNBT serializeNBT() {
        if (! isNormalized()) {
            BuildingGadgets.LOGGER.warn("Attempted to serialize denormal Template. This should never happen!");
            return normalize().serializeNBT();
        }

        BuildingGadgets.LOGGER.trace("Serializing {}'s Template of size {}.", author, data.size());
        CompoundNBT res = new CompoundNBT();
        Object2IntMap<BlockState> allStates = new Object2IntLinkedOpenHashMap<>(); //linked to keep the order
        Object2IntMap<CompoundNBT> allTiles = new Object2IntLinkedOpenHashMap<>();
        int stateCounter = 0;
        int tileCounter = 0;
        LongList posStateList = new LongArrayList(data.size());
        LongList posTileDataList = new LongArrayList();

        //These sets are purely for logging and will rarely be filled...
        Set<BlockState> statesOutOfRange = new HashSet<>();
        Set<CompoundNBT> tilesOutOfRange = new HashSet<>();
        //Let's create all the longs necessary for the pos->state mapping
        for (TemplateData entry : data) {
            long pos = vecToLong(entry.getPos());
            stateCounter = addInfo(entry.getData().getState(), allStates, stateCounter, statesOutOfRange, LOG_STATES);
            int stateId = allStates.getInt(entry.getData().getState());
            posStateList.add(includeId(pos, stateId));
            if (entry.getData().getNbt() != null) {
                tileCounter = addInfo(entry.getData().getNbt(), allTiles, tileCounter, tilesOutOfRange, LOG_TILES);
                int tileId = allTiles.getInt(entry.getData().getNbt());
                posTileDataList.add(includeId(pos, tileId));
            }
        }
        //Now transform the data
        ListNBT uniqueStates = allStates.keySet().stream()
                .map(NBTUtil::writeBlockState)
                .collect(LIST_COLLECTOR);
        ListNBT uniqueTiles = allTiles.keySet().stream().collect(LIST_COLLECTOR);

        //and finally we can put it all into the compound
        res.putLongArray(KEY_STATE_POS, posStateList.toLongArray());
        res.put(KEY_STATE_DATA, uniqueStates);
        if (! posTileDataList.isEmpty()) {
            res.putLongArray(KEY_TILE_POS, posTileDataList.toLongArray());
            res.put(KEY_TILE_DATA, uniqueTiles);
        }
        res.putString(KEY_AUTHOR, author);

        return res;
    }

    private static <T> int addInfo(T obj, Object2IntMap<T> idMap, int idCount, Set<T> outOfRange, String logName) {
        if (! idMap.containsKey(obj)) {
            if (idCount > MAX_NUMBER_OF_IDS) {
                if (! outOfRange.contains(obj)) {
                    BuildingGadgets.LOGGER.error("Too many {}} to be stored in a single Template!!! " +
                                    "{} would get id {} which is out of range {0,...,{}}!", logName, obj, idCount++,
                            MAX_NUMBER_OF_IDS);
                    outOfRange.add(obj);
                }
                return idCount;
            }
            idMap.put(obj, idCount++);
        }
        return idCount;
    }

    /**
     * Consume the {@code Template} by iterating over it. Notice that some data has been removed from the tile data passed
     * to {@link Builder#recordBlock(BlockPos, BlockState, CompoundNBT)} and needs to be re-added.
     *
     * @return An {@link Iterator} over this Template's elements, iterating the elements in YXZ order.
     */
    @Override
    public Iterator<TemplateData> iterator() {
        return data.iterator();
    }

    @Override
    public Spliterator<TemplateData> spliterator() {
        return data.spliterator();
    }

    public Stream<TemplateData> stream() {
        return data.stream();
    }

    public Template rotate(Axis axis, Rotation rotation) {
        int[][] mat = rotationMatrixFor(axis, rotation);
        Function<BlockData, BlockData> transform = axis == Axis.Y ? s -> s.rotate(rotation) : Function.identity();
        return matrixOp(mat, transform);
    }

    public Template mirror(Axis axis) {
        int[][] mat = mirrorMatrixFor(axis);
        Mirror m = axis == Axis.X ? Mirror.LEFT_RIGHT : axis == Axis.Z ? Mirror.FRONT_BACK : Mirror.NONE;
        Function<BlockData, BlockData> transform = m != Mirror.NONE ? b -> b.mirror(m) : Function.identity();
        return matrixOp(mat, transform);
    }

    /**
     * @return A Template that is normalized to the bounds [(0,0,0), (maxX, maxY, maxZ)]. The resulting Template will also
     * be sorted in (x,y,z) order.
     */
    private Template normalize() {
        if (isNormalized())
            return this;
        //notice that this still has O(n) time complexity: translation does not change the order thus Arrays#sort, which
        //is used under the hood, will sort the already sorted array in O(n)
        return matrixOp(IDENTITY_3X3, Function.identity());
    }

    private boolean isNormalized() {
        return bounds.createMinPos().equals(BlockPos.ZERO);
    }

    private Template matrixOp(int[][] matrix3x3, Function<BlockData, BlockData> dataTransform) {
        BlockPos min = matrixMul(matrix3x3, BlockPos.ZERO, bounds.createMinPos());
        BlockPos max = matrixMul(matrix3x3, BlockPos.ZERO, bounds.createMaxPos());
        BoundingBox transformed = new BoundingBox(min, max);
        BlockPos translation = transformed.createMinPos();
        //BuildingGadgets.LOGGER.info("Transformed {} into {}. Applying translation {}.", bounds, transformed, translation);
        ImmutableSortedSet<TemplateData> map = data.stream()
                .map(td -> new TemplateData(matrixMul(matrix3x3, translation, td.getPos()), dataTransform.apply(td.getData())))
                .collect(ImmutableSortedSet.toImmutableSortedSet(BY_POS_YXZ));

        return new Template(map, new BoundingBox(BlockPos.ZERO, transformed.createMaxPos().subtract(translation)), author);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bounds", bounds)
                .add("author", author)
                .add("size", data.size())
                .toString();
    }

    public static final class Builder {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;
        private final BlockPos translationPos;
        private final ImmutableMap.Builder<BlockPos, BlockData> builder;
        private String author;

        /**
         * @see #builder(BlockPos)
         */
        private Builder(BlockPos translationPos) {
            this.translationPos = translationPos;
            this.builder = ImmutableMap.builder();
            this.author = "";
            this.minX = this.minY = this.minZ = Integer.MAX_VALUE;
            this.maxX = this.maxY = this.maxZ = Integer.MIN_VALUE;
        }

        /**
         * Set the author of the resulting {@link Template}. Defaults to {@code ""}.
         *
         * @param author The author of the resulting {@link Template}
         * @return The {@code Builder} instance for Method chaining.
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Record the given block in the resulting Template. This will also strip unnecessary data from a copy of the
         * tile data. The tags {@code "x"}, {@code "y"}, , {@code "z"} and {@code "id"} will be removed as this
         * information would be either wrong or redundant when placing a tile from a Template.
         *
         * @param pos   The real-world pos at which {@code state} was found.
         * @param state The {@link BlockState} to record.
         * @param tileNBT Tile data if present or null else. Will not be modified.
         * @return The {@code Builder} instance for Method chaining.
         */
        public Builder recordBlock(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNBT) {
            assert state.hasTileEntity() || tileNBT == null;
            BlockPos key = pos.subtract(translationPos).toImmutable();
            if (tileNBT != null) {
                tileNBT = tileNBT.copy();
                //remove data that is not relevant to our purpose
                tileNBT.remove("x");
                tileNBT.remove("y");
                tileNBT.remove("z");
                //remove this is as well. We place the blockstate and then let the world create the tile for us
                //thus the id can also be reapplied, as we'll have the tile at hand
                tileNBT.remove("id");
                if (tileNBT.isEmpty()) //this data is therefore not needed
                    tileNBT = null;
            }
            builder.put(key, new BlockData(state, tileNBT));
            minX = Math.min(key.getX(), minX);
            minY = Math.min(key.getY(), minY);
            minZ = Math.min(key.getZ(), minZ);
            maxX = Math.max(key.getX(), maxX);
            maxY = Math.max(key.getY(), maxY);
            maxZ = Math.max(key.getZ(), maxZ);
            return this;
        }

        /**
         * @return The {@link Template} which is represented by this Builder
         */
        public Template build() {
            assert author != null;
            ImmutableMap<BlockPos, BlockData> dataMap = builder.build();
            BoundingBox bounds = dataMap.isEmpty() ? BoundingBox.ZEROS : new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
            BlockPos minPos = bounds.createMinPos();
            if (! minPos.equals(BlockPos.ZERO))
                bounds = new BoundingBox(BlockPos.ZERO, bounds.createMaxPos().subtract(minPos));
            ImmutableSortedSet<TemplateData> data = dataMap.entrySet()
                    .stream()
                    .map(e -> new TemplateData(e.getKey().subtract(minPos), e.getValue()))
                    .collect(ImmutableSortedSet.toImmutableSortedSet(BY_POS_YXZ));
            //No need to normalize as we do that already above in a single step
            return new Template(data, bounds, author);
        }
    }
}
