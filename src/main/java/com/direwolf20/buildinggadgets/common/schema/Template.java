package com.direwolf20.buildinggadgets.common.schema;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;

import java.util.Iterator;

public final class Template implements Iterable<TemplateData> {
    private static final int BYTE_MASK_12BIT = 0xF_FF;
    private static final int BYTE_MASK_14BIT = 0x3F_FF;
    private final Long2ObjectMap<BlockState> states;
    private final Long2ObjectMap<CompoundNBT> nbtData;
    private final BoundingBox bounds;
    private final String author;

    private Template(Long2ObjectMap<BlockState> states, Long2ObjectMap<CompoundNBT> nbtData,
                     BoundingBox bounds, String author) {
        this.states = states;
        this.nbtData = nbtData;
        this.bounds = bounds;
        this.author = author;
    }

    /**
     * Converts a {@link Vec3i} into a long representation. Notice that only the first 40bits are used, because the
     * remaining 24bit are reserved for state-id's. This is also why {@link BlockPos#toLong()} cannot be used.
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
     * @param setTo the {@link Mutable} to apply values to
     * @param serialized the long produced by {@link #vecToLong(Vec3i)}
     * @return {@code setTo} to allow for Method chainging.
     */
    private Mutable posFromLong(Mutable setTo, long serialized) {
        int x = (int) ((serialized >> 26) & BYTE_MASK_14BIT);
        int y = (int) ((serialized >> 14) & BYTE_MASK_12BIT);
        int z = (int) (serialized & BYTE_MASK_14BIT);
        setTo.setPos(x, y, z);
        return setTo;
    }

    /**
     * <b>Warning:</b> do not store the {@link TemplateData} without invoking {@link TemplateData#copy()} first.
     * This implementation uses only a single {@link TemplateData} and a single {@link Mutable} instance during the
     * iteration which are mutated to fit to the current iteration point. Therefore a new invocation of
     * {@link Iterator#next()} will reset the data viewed by a reference to this {@link Iterator}'s result.
     * <p>
     * This implementation provides {@code O(1)} Object allocations during during iteration in order to be usable with
     * large Templates without over-utilising the garbage collector. For an idea of the performance of this mutable variant
     * compared to recreating the Object in each step, refer to the classical {@link String} concat test (for example a
     * variant of this can be seen in the second answer of this:
     * https://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java).
     * @return An {@link Iterator} of the {@link TemplateData} represented by this Template in X-Y-Z order.
     */
    @Override
    public Iterator<TemplateData> iterator() {
        return new AbstractIterator<TemplateData>() {
            private final ObjectIterator<Entry<BlockState>> it = states.long2ObjectEntrySet().iterator();
            private TemplateData theData = null;
            private Mutable thePos = null;
            @Override
            protected TemplateData computeNext() {
                if (!it.hasNext())
                    return endOfData();
                Entry<BlockState> entry = it.next();
                thePos = posFromLong(thePos != null ? thePos : new Mutable(), entry.getLongKey());
                CompoundNBT nbt = nbtData.get(entry.getLongKey());
                if (theData == null) {
                    theData = new TemplateData(thePos, entry.getValue(), nbt);
                    return theData;
                }
                return theData.setInformation(thePos, entry.getValue(), nbt);
            }
        };
    }
}
