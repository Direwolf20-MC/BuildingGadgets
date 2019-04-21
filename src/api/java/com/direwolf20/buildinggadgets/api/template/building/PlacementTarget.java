package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * {@link BlockPos} and {@link BlockData} combined, to allow for placement of an {@link BlockData} in an {@link IBuildContext}. Ths class also offers serialisation
 * capabilities, though in general mapping positions to data across a structure wide-map should be preferred, as BlockData:position is a 1:n relationship.
 * <p>
 * Notice that this class is immutable as long as the {@link ITileEntityData} instance of the contained {@link BlockData} is immutable.
 */
public final class PlacementTarget {
    private static final String KEY_DATA = "data";
    private static final String KEY_POS = "pos";

    /**
     * @param nbt The {@link NBTTagCompound} representing the serialized form of this {@code PlacementTarget}.
     * @param persisted Flag indicating whether the data was created for an persisted save or not
     * @return A new {@code PlacementTarget} representing the serialized form of nbt.
     * @throws IllegalArgumentException if the persisted flag does not match the way the nbt was created
     * @throws NullPointerException if the serializer for the {@link ITileEntityData} could not be found
     * @see BlockData#deserialize(NBTTagCompound, boolean)
     */
    public static PlacementTarget deserialize(NBTTagCompound nbt, boolean persisted) {
        BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound(KEY_POS));
        BlockData data = BlockData.deserialize(nbt.getCompound(KEY_DATA), persisted);
        return new PlacementTarget(pos, data);
    }

    private final BlockPos pos;
    private final BlockData data;

    /**
     * Creates a new {@code PlacementTarget} for the specified position and data
     * @param pos The {@link BlockPos} at which the data should be placed
     * @param data The {@link BlockData} to be placed
     * @throws NullPointerException if position or data are null
     */
    public PlacementTarget(@Nonnull BlockPos pos, @Nonnull BlockData data) {
        this.pos = Objects.requireNonNull(pos);
        this.data = Objects.requireNonNull(data);
    }

    /**
     * @return The {@link BlockPos} at which something should be placed
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * @return The {@link BlockData} to be placed by this {@code PlacementTarget}.
     */
    public BlockData getData() {
        return data;
    }

    /**
     * Attempts to place the {@link BlockData} at the specified position.
     * @param context The {@link IBuildContext} to place in.
     * @return Whether or not placement was performed by the underlying {@link BlockData}
     * @see BlockData#placeIn(IBuildContext, BlockPos)
     */
    public boolean placeIn(IBuildContext context) {
        return data.placeIn(context, pos);
    }

    /**
     * Serializes the data contained by this {@link PlacementTarget}. The persisted flag is used as an hint, whether non-persistent formats (Registry-id's) may be used to
     * reduce the size of the resulting {@link NBTTagCompound}.
     * @param persisted Whether or not this should be created as an persisted save.
     * @return The serialized form of this {@code PlacementTarget} as an {@link NBTTagCompound}.
     * @see BlockData#serialize(boolean)
     */
    public NBTTagCompound serialize(boolean persisted) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(KEY_DATA, data.serialize(persisted));
        compound.setTag(KEY_POS, NBTUtil.writeBlockPos(pos));
        return compound;
    }
}
