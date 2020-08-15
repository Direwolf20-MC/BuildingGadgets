package com.direwolf20.buildinggadgets.common.schema.template;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.schema.BoundingBox;
import com.direwolf20.buildinggadgets.common.schema.template.TemplateData.BlockData;
import com.google.common.collect.ImmutableSortedMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public final class Template implements Iterable<TemplateData> {
    private static final String KEY_POS = "pos";
    private static final String KEY_DATA = "data";
    private static final String KEY_AUTHOR = "author";
    private static final int BYTE_MASK_12BIT = 0xF_FF;
    private static final int BYTE_MASK_14BIT = 0x3F_FF;
    private static final int BYTE_MASK_24BIT = 0xFF_FF_FF;
    private static final long BYTE_MASK_40BIT = ((long) 0xFF_FF_FF_FF) << 8 | 0xFF;
    private static final int MAX_NUMBER_OF_STATES = (1 << 24) - 1;
    private static final Comparator<BlockPos> YXZ_COMPARATOR =
            Comparator.comparing(BlockPos::getY).thenComparing(BlockPos::getX).thenComparing(BlockPos::getZ);
    private static final int[][] IDENTITY_3X3 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    private final ImmutableSortedMap<BlockPos, BlockData> data;
    private final BoundingBox bounds;
    private final String author;

    private Template(ImmutableSortedMap<BlockPos, BlockData> data, BoundingBox bounds, String author) {
        this.data = data;
        this.bounds = bounds;
        this.author = author;
    }

    public static Optional<Template> deserializeNBT(CompoundNBT nbt) {
        if (! nbt.contains(KEY_POS, NBT.TAG_LONG_ARRAY) || ! nbt.contains(KEY_DATA, NBT.TAG_LIST))
            return Optional.empty();

        //read the states
        ListNBT nbtData = (ListNBT) nbt.get(KEY_DATA);
        assert nbtData != null;
        List<BlockData> uniqueData = new ArrayList<>(nbtData.size());
        for (INBT dataNBT : nbtData) {
            //read blockstate will replace everything unknown with the default value for Blocks: AIR
            BlockData data = BlockData.deserialize((CompoundNBT) dataNBT);
            //a trace log, as this may be normal... In the future we might want to replace this with some placeholder
            if (data.getState() == Blocks.AIR.getDefaultState())
                BuildingGadgets.LOGGER.trace("Found unknown state with nbt {}. Will be ignored!", dataNBT);
            uniqueData.add(data);
        }
        //process the state map - need the explicit type in order to be able to trim
        ImmutableSortedMap.Builder<BlockPos, BlockData> builder = ImmutableSortedMap.orderedBy(YXZ_COMPARATOR);
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        for (long packed : nbt.getLongArray(KEY_POS)) {
            int id = getId(packed);
            long posLong = getPosLong(packed);
            BlockPos thePos = posFromLong(posLong);
            if (id >= uniqueData.size() || id < 0) {
                BuildingGadgets.LOGGER.error("Found illegal state-id {} in Template at pos {}(={}), when {} states " +
                                "are known. This indicates a greater Problem and the Template is therefore deemed broken.",
                        id, getPosLong(packed), thePos, uniqueData.size());
                return Optional.empty();
            }

            BlockData data = uniqueData.get(id);
            //Skip unknown Blocks - this has no log, as it is already logged before and even might happen more
            //than once. So having a log here might result in serious log spam
            if (data.getState() == Blocks.AIR.getDefaultState())
                continue;
            //I'd actually like to outsource this to a class, but Mike told me not to... (see Builder#recordBlock)
            minX = Math.min(thePos.getX(), minX);
            minY = Math.min(thePos.getY(), minY);
            minZ = Math.min(thePos.getZ(), minZ);
            maxX = Math.max(thePos.getX(), maxX);
            maxY = Math.max(thePos.getY(), maxY);
            maxZ = Math.max(thePos.getZ(), maxZ);

            builder.put(thePos, data);
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
        Object2IntMap<BlockData> allStates = new Object2IntLinkedOpenHashMap<>(); //linked to keep the order
        int stateCounter = 0;
        LongList posStateList = new LongArrayList(data.size());

        //These sets are purely for logging and will rarely be filled...
        Set<BlockData> dataOutOfRange = new HashSet<>();
        //Let's create all the longs necessary for the pos->state mapping
        for (Entry<BlockPos, BlockData> entry : data.entrySet()) {
            if (! allStates.containsKey(entry.getValue())) {
                if (stateCounter > MAX_NUMBER_OF_STATES) {
                    if (! dataOutOfRange.contains(entry.getValue())) {
                        BuildingGadgets.LOGGER.error("Too many states to be stored in a single Template!!! " +
                                        "{} would get id {} which is out of range {0,...,{}}!", entry.getValue(), stateCounter++,
                                MAX_NUMBER_OF_STATES);
                        dataOutOfRange.add(entry.getValue());
                    }
                    continue;
                }
                allStates.put(entry.getValue(), stateCounter++);
            }
            int stateId = allStates.getInt(entry.getValue());
            posStateList.add(includeId(vecToLong(entry.getKey()), stateId));
        }
        //Now transform the states
        ListNBT uniqueStates = allStates.keySet().stream()
                .map(BlockData::serialize)
                .collect(NBTHelper.LIST_COLLECTOR);

        //and finally we can put it all into the compound
        res.putLongArray(KEY_POS, posStateList.toLongArray());
        res.put(KEY_DATA, uniqueStates);
        res.putString(KEY_AUTHOR, author);

        return res;
    }

    /**
     * Warning: do not store the {@link TemplateData} without invoking {@link TemplateData#copy()} first.
     * This implementation uses only a single {@link TemplateData} and a single {@link Mutable} instance during the
     * iteration which are mutated to fit to the current iteration point. Therefore a new invocation of
     * {@link Iterator#next()} will reset the data viewed by a reference to this {@link Iterator}'s result.
     *
     * This implementation provides {@code O(1)} Object allocations during during iteration (assuming no tile data
     * to be present) in order to be usable with large Templates without over-utilising the garbage collector.
     * For an idea of the performance of this mutable variant compared to recreating the Object in each step,
     * refer to the classical {@link String} concat test (for example a variant of this can be seen in the second
     * answer of this:
     * https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java).
     *
     * @return An {@link Iterator} of the {@link TemplateData} represented by this Template in X-Y-Z order.
     */
    @Override
    public Iterator<TemplateData> iterator() {
        return new Iterator<TemplateData>() {
            private final Iterator<Entry<BlockPos, BlockData>> it = data.entrySet().iterator();
            private TemplateData theData = null;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public TemplateData next() {
                Entry<BlockPos, BlockData> entry = it.next();

                if (theData == null)
                    theData = new TemplateData(entry.getKey(), entry.getValue());

                return theData.setInformation(entry.getKey(), entry.getValue());
            }
        };
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

        ImmutableSortedMap<BlockPos, BlockData> map = data.entrySet().stream()
                .map(e -> Pair.of(matrixMul(matrix3x3, translation, e.getKey()), dataTransform.apply(e.getValue())))
                .collect(ImmutableSortedMap.toImmutableSortedMap(YXZ_COMPARATOR, Pair::getFirst, Pair::getSecond));

        return new Template(map, new BoundingBox(BlockPos.ZERO, transformed.createMaxPos().subtract(translation)), author);
    }

    public static final class Builder {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;
        private final BlockPos translationPos;
        private final ImmutableSortedMap.Builder<BlockPos, BlockData> builder;
        private String author;

        /**
         * @see #builder(BlockPos)
         */
        private Builder(BlockPos translationPos) {
            this.translationPos = translationPos;
            this.builder = ImmutableSortedMap.orderedBy(YXZ_COMPARATOR);
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
         * Record the given block in the resulting Template.
         *
         * @param pos   The real-world pos at which {@code state} was found.
         * @param state The {@link BlockState} to record.
         * @param tileNBT tile data if present or null else
         * @return The {@code Builder} instance for Method chaining.
         */
        public Builder recordBlock(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNBT) {
            assert state.hasTileEntity() || tileNBT == null;
            BlockPos key = pos.subtract(translationPos).toImmutable();
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
            ImmutableSortedMap<BlockPos, BlockData> map = builder.build();
            BoundingBox bounds = map.isEmpty() ? BoundingBox.ZEROS : new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
            return new Template(map, bounds, author).normalize();
        }
    }
}
