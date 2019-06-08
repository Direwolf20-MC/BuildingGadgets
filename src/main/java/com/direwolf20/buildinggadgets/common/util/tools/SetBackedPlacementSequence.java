package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

public class SetBackedPlacementSequence implements IPositionPlacementSequence, Serializable {

    private static final long serialVersionUID = 5752016672792062860L;

    private Region boundingBox;
    private HashSet<BlockPos> internalSet;

    public SetBackedPlacementSequence(HashSet<BlockPos> internalSet, Region boundingBox) {
        this.internalSet = internalSet;
        this.boundingBox = boundingBox;
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
     * Warning: this method uses the copy constructor of {@link HashSet}, therefore it does not guarantee it will return
     * the same type of set.
     */
    @Deprecated
    @Nonnull
    @Override
    public IPositionPlacementSequence copy() {
        return new SetBackedPlacementSequence(new HashSet<>(internalSet), boundingBox);
    }

    public HashSet<BlockPos> getInternalSet() {
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
