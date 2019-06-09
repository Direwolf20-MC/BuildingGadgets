package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

public class SetBackedPlacementSequence implements IPositionPlacementSequence, Serializable {

    private static final long serialVersionUID = 3372886441086287285L;

    private Region boundingBox;
    private Set<BlockPos> internalSet;

    /**
     * @param internalSet The set that contains the {@link BlockPos}s this sequence contains. Notice this will be
     *                    wrapped with {@link Collections#unmodifiableSet(Set)}.
     * @param boundingBox Bounding box of all {@link BlockPos} contained in {@code internalSet}.
     */
    public SetBackedPlacementSequence(@Nonnull Set<BlockPos> internalSet, @Nonnull Region boundingBox) {
        this.internalSet = Objects.requireNonNull(Collections.unmodifiableSet(internalSet));
        this.boundingBox = Objects.requireNonNull(boundingBox);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return internalSet.iterator();
    }

    @Nonnull
    @Override
    public Region getBoundingBox() {
        return boundingBox;
    }

    /**
     * @deprecated Use {@link #contains(int, int, int)} instead.
     */
    @Deprecated
    @Override
    public boolean mayContain(int x, int y, int z) {
        return contains(new BlockPos(x, y, z));
    }

    public boolean contains(int x, int y, int z) {
        return contains(new BlockPos(x, y, z));
    }

    public boolean contains(BlockPos pos) {
        return internalSet.contains(pos);
    }

    /**
     * <b>WARNING</b>: this method uses the copy constructor of {@link LinkedHashSet}, therefore it does not guarantee
     * it will return the same type of set.
     */
    @Nonnull
    @Override
    public IPositionPlacementSequence copy() {
        return new SetBackedPlacementSequence(new LinkedHashSet<>(internalSet), boundingBox);
    }

    /**
     * Returns te set passed into the constructor as {@code internalSet} wrapped with {@link
     * Collections#unmodifiableSet(Set)}.
     */
    public Set<BlockPos> getInternalSet() {
        return internalSet;
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        boundingBox = (Region) in.readObject();
        internalSet = new HashSet<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            internalSet.add(new BlockPos(in.readInt(), in.readInt(), in.readInt()));
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(boundingBox);
        out.writeInt(internalSet.size());
        for (BlockPos pos : internalSet) {
            out.writeInt(pos.getX());
            out.writeInt(pos.getY());
            out.writeInt(pos.getZ());
        }
    }

}
