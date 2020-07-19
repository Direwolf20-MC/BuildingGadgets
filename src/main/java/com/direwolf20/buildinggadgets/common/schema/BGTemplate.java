package com.direwolf20.buildinggadgets.common.schema;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;

import java.util.Iterator;

public final class BGTemplate implements Iterable<MutableBlockData> {
    private static final int BYTE_MASK_12BIT = 0xF_FF;
    private static final int BYTE_MASK_14BIT = 0x3F_FF;
    private final Long2ObjectMap<BlockState> states;
    private final Long2ObjectMap<CompoundNBT> nbtData;
    private final BoundingBox bounds;
    private final String author;
    private final String name;

    private BGTemplate(Long2ObjectMap<BlockState> states, Long2ObjectMap<CompoundNBT> nbtData, BoundingBox bounds,
                      String author, String name) {
        this.states = states;
        this.nbtData = nbtData;
        this.bounds = bounds;
        this.author = author;
        this.name = name;
    }

    private long vecToLong(Vec3i vec) {
        long res = (long) (vec.getX() & BYTE_MASK_14BIT) << 26;
        res |= (vec.getY() & BYTE_MASK_12BIT) << 14;
        res |= (vec.getZ() & BYTE_MASK_14BIT);
        return res;
    }

    private Mutable posFromLong(Mutable setTo, long serialized) {
        int x = (int) ((serialized >> 26) & BYTE_MASK_14BIT);
        int y = (int) ((serialized >> 14) & BYTE_MASK_12BIT);
        int z = (int) (serialized & BYTE_MASK_14BIT);
        setTo.setPos(x, y, z);
        return setTo;
    }

    @Override
    public Iterator<MutableBlockData> iterator() {
        return new AbstractIterator<MutableBlockData>() {
            private final ObjectIterator<Entry<BlockState>> it = states.long2ObjectEntrySet().iterator();
            private MutableBlockData theData = null;
            private Mutable thePos = null;
            @Override
            protected MutableBlockData computeNext() {
                if (!it.hasNext())
                    return endOfData();
                Entry<BlockState> entry = it.next();
                thePos = posFromLong(thePos != null ? thePos : new Mutable(), entry.getLongKey());
                CompoundNBT nbt = nbtData.get(entry.getLongKey());
                if (theData == null) {
                    theData = new MutableBlockData(thePos, entry.getValue(), nbt);
                    return theData;
                }
                return theData.setInformation(thePos, entry.getValue(), nbt);
            }
        };
    }
}
