package com.direwolf20.buildinggadgets.common.schema.template;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.schema.BoundingBox;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;

public final class Template implements Iterable<TemplateData> {
    private static final String KEY_POS = "pos";
    private static final String KEY_DATA = "data";
    private static final String KEY_AUTHOR = "author";
    private static final int BYTE_MASK_12BIT = 0xF_FF;
    private static final int BYTE_MASK_14BIT = 0x3F_FF;
    private static final int BYTE_MASK_24BIT = 0xFF_FF_FF;
    private static final long BYTE_MASK_40BIT = ((long) 0xFF_FF_FF_FF) << 8 | 0xFF;
    private static final int MAX_NUMBER_OF_STATES = (1 << 24) - 1;
    private final Long2ObjectMap<BlockState> states;
    private final BoundingBox bounds;
    private final String author;

    public static Optional<Template> deserializeNBT(CompoundNBT nbt) {
        if (! nbt.contains(KEY_POS, NBT.TAG_LONG_ARRAY) || ! nbt.contains(KEY_DATA, NBT.TAG_LIST))
            return Optional.empty();

        Long2ObjectMap<BlockState> states = new Long2ObjectLinkedOpenHashMap<>();
        BoundingBox bounds = null;
        String author = nbt.getString(KEY_AUTHOR);

        //read the states
        ListNBT nbtStates = (ListNBT) nbt.get(KEY_DATA);
        assert nbtStates != null;
        List<BlockState> uniqueStates = new ArrayList<>(nbtStates.size());
        for (INBT stateNBT : nbtStates) {
            //read blockstate will replace everything unknown with the default value for Blocks: AIR
            BlockState state = NBTUtil.readBlockState((CompoundNBT) stateNBT);
            //a trace log, as this may be normal... In the future we might want to replace this with some placeholder
            if (state == Blocks.AIR.getDefaultState())
                BuildingGadgets.LOGGER.trace("Found unknown state with nbt {}. Will be ignored!", stateNBT);
            uniqueStates.add(state);
        }
        //process the state map
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 0;
        Mutable thePos = new Mutable();
        for (long packed : nbt.getLongArray(KEY_POS)) {
            int id = getId(packed);
            if (id >= uniqueStates.size() || id < 0) {
                BuildingGadgets.LOGGER.error("Found illegal state-id {} in Template at pos {}(={}), when {} states " +
                                "are known. This indicates a greater Problem and the Template is therefore deemed broken.",
                        id, getPos(packed), posFromLong(new Mutable(), getPos(packed)), uniqueStates.size());
                return Optional.empty();
            }
            BlockState state = uniqueStates.get(id);
            if (state == Blocks.AIR.getDefaultState())
                //Skip unknown Blocks - this has no log, as it is already logged before and even might happen more
                //than once. So having a log here might result in serious log spam
                continue;
            long posLong = getPos(packed);
            posFromLong(thePos, posLong);
            //I'd actually like to outsource this to a class, but Mike told me not to...
            minX = Math.min(thePos.getX(), minX);
            minY = Math.min(thePos.getY(), minY);
            minZ = Math.min(thePos.getZ(), minZ);
            maxX = Math.max(thePos.getX(), maxX);
            maxY = Math.max(thePos.getY(), maxY);
            maxZ = Math.max(thePos.getZ(), maxZ);

            states.put(posLong, state);
        }
        //re-evaluate the bounding box based on potentially missing blocks
        bounds = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        //The Template may no longer have 0,0,0 min - if the corresponding blocks had to be removed
        //Therefore normalize the result.
        return Optional.of(new Template(states, bounds, author))
                .map(Template::normalize);
    }

    private static int getId(long packed) {
        return (int) ((packed >> 40) & BYTE_MASK_24BIT);
    }

    private static long getPos(long packed) {
        return packed & BYTE_MASK_40BIT;
    }

    private Template(Long2ObjectMap<BlockState> states, BoundingBox bounds, String author) {
        this.states = states;
        this.bounds = bounds;
        this.author = author;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getAuthor() {
        return author;
    }

    public int size() {
        return states.size();
    }

    /**
     * Converts a {@link Vec3i} into a long representation. Notice that only the first 40bits are used, because the
     * remaining 24bit are reserved for state-id's. This is also why {@link BlockPos#toLong()} cannot be used.
     *
     * @param vec The vector to "longify"
     * @return A long consisting of [24bit-unused]|[14bit-x_coord]|[12bit-y_coord]|[14bit-z_coord]
     */
    private long vecToLong(Vec3i vec) {
        long res = (long) (vec.getX() & BYTE_MASK_14BIT) << 26;
        res |= (vec.getY() & BYTE_MASK_12BIT) << 14;
        res |= (vec.getZ() & BYTE_MASK_14BIT);
        return res;
    }

    /**
     * Converts longs provided by {@link #vecToLong(Vec3i)} back into a {@link Mutable MutableBlockPos}.
     *
     * @param setTo      the {@link Mutable} to apply values to
     * @param serialized the long produced by {@link #vecToLong(Vec3i)}
     * @return {@code setTo} to allow for Method changing.
     */
    private static Mutable posFromLong(Mutable setTo, long serialized) {
        int x = (int) ((serialized >> 26) & BYTE_MASK_14BIT);
        int y = (int) ((serialized >> 14) & BYTE_MASK_12BIT);
        int z = (int) (serialized & BYTE_MASK_14BIT);
        setTo.setPos(x, y, z);
        return setTo;
    }

    private long includeId(long posLong, int stateId) {
        return posLong | ((long) (stateId & BYTE_MASK_24BIT) << 40);
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
            private final ObjectIterator<Entry<BlockState>> it = states.long2ObjectEntrySet().iterator();
            private TemplateData theData = null;
            private Mutable thePos = null;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public TemplateData next() {
                Entry<BlockState> entry = it.next();
                thePos = posFromLong(thePos != null ? thePos : new Mutable(), entry.getLongKey());

                if (theData == null) {
                    theData = new TemplateData(thePos, entry.getValue());
                    return theData;
                }

                return theData.setInformation(thePos, entry.getValue());
            }
        };
    }

    /**
     * @return A Template that is normalized to the bounds [(0,0,0), (maxX, maxY, maxZ)]
     */
    public Template normalize() {
        BlockPos min = bounds.getMinPos();
        if (min.equals(BlockPos.ZERO))
            return this;

        int translationX = - min.getX();
        int translationY = - min.getY();
        int translationZ = - min.getZ();
        Long2ObjectMap<BlockState> transformedStates = new Long2ObjectOpenHashMap<>();
        Mutable pos = new Mutable();
        for (Entry<BlockState> entry : states.long2ObjectEntrySet())
            transformedStates.put(
                    vecToLong(posFromLong(pos, entry.getLongKey()).move(translationX, translationY, translationZ)),
                    entry.getValue()
            );

        return new Template(transformedStates, new BoundingBox(BlockPos.ZERO, bounds.getMaxPos().subtract(min)), author);
    }

    public CompoundNBT serializeNBT() {
        BuildingGadgets.LOGGER.trace("Serializing {}'s Template of size {}.", author, states.size());
        CompoundNBT res = new CompoundNBT();
        Object2IntMap<BlockState> allStates = new Object2IntLinkedOpenHashMap<>(); //linked to keep the order
        int stateCounter = 0;
        LongList posStateList = new LongArrayList(states.size());

        //These sets are purely for logging and will rarely be filled...
        Set<BlockState> statesOutOfRange = Collections.newSetFromMap(new IdentityHashMap<>()); //states can do with reference equality
        //Let's create all the longs necessary for the pos->state mapping
        for (Entry<BlockState> entry : states.long2ObjectEntrySet()) {
            if (! allStates.containsKey(entry.getValue())) {
                if (stateCounter > MAX_NUMBER_OF_STATES) {
                    if (! statesOutOfRange.contains(entry.getValue())) {
                        BuildingGadgets.LOGGER.error("Too many states to be stored in a single Template!!! " +
                                        "{} would get id {} which is out of range {0,...,{}}!", entry.getValue(), stateCounter++,
                                MAX_NUMBER_OF_STATES);
                        statesOutOfRange.add(entry.getValue());
                    }
                    continue;
                }
                allStates.put(entry.getValue(), stateCounter++);
            }
            int stateId = allStates.getInt(entry.getValue());
            posStateList.add(includeId(entry.getLongKey(), stateId));
        }
        //Now transform the states
        ListNBT uniqueStates = allStates.keySet().stream()
                .map(NBTUtil::writeBlockState)
                .collect(NBTHelper.LIST_COLLECTOR);

        //and finally we can put it all into the compound
        res.putLongArray(KEY_POS, posStateList.toLongArray());
        res.put(KEY_DATA, uniqueStates);
        res.putString(KEY_AUTHOR, author);

        return res;
    }
}
