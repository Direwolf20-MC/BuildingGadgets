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

    public SetBackedPlacementSequence(Set<BlockPos> internalSet, Region boundingBox) {
        this.internalSet = Objects.requireNonNull(internalSet);
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
     * <b>WARNING</b>: this method uses the copy constructor of {@link HashSet}, therefore it does not guarantee it will return
     * the same type of set.
     */
    @Nonnull
    @Override
    public IPositionPlacementSequence copy() {
        return new SetBackedPlacementSequence(new HashSet<>(internalSet), boundingBox);
    }

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
