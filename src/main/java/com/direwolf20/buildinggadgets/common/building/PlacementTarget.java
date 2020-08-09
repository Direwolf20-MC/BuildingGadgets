package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.MoreObjects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * {@link BlockPos} and {@link BlockData} combined, to allow for placement of an {@link BlockData} in an {@link BuildContext}. Ths class also offers serialisation
 * capabilities, though in general mapping positions to data across a structure wide-map should be preferred, as BlockData:position is a 1:n relationship.
 * <p>
 * Notice that this class is immutable as long as the {@link ITileEntityData} instance of the contained {@link BlockData} is immutable.
 */
public final class PlacementTarget {

    /**
     * @param nbt The {@link CompoundNBT} representing the serialized form of this {@code PlacementTarget}.
     * @param persisted Flag indicating whether the data was created for an persisted save or not
     * @return A new {@code PlacementTarget} representing the serialized form of nbt.
     * @throws IllegalArgumentException if the persisted flag does not match the way the nbt was created
     * @throws NullPointerException if the serializer for the {@link ITileEntityData} could not be found
     * @see BlockData#deserialize(CompoundNBT, boolean)
     */
    public static PlacementTarget deserialize(CompoundNBT nbt, boolean persisted) {
        BlockPos pos = NBTUtil.readBlockPos(nbt.getCompound(NBTKeys.KEY_POS));
        BlockData data = BlockData.deserialize(nbt.getCompound(NBTKeys.KEY_DATA), persisted);
        return new PlacementTarget(pos, data);
    }

    @Nonnull
    private final BlockPos pos;
    @Nonnull
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
     * @param context The {@link BuildContext} to place in.
     * @return Whether or not placement was performed by the underlying {@link BlockData}
     * @see BlockData#placeIn(BuildContext, BlockPos)
     */
    public boolean placeIn(BuildContext context) {
        return data.placeIn(context, pos);
    }

    public PlacementTarget mirror(Mirror mirror) {
        return new PlacementTarget(pos, data.mirror(mirror));
    }

    public PlacementTarget rotate(Rotation rotation) {
        return new PlacementTarget(getPos(), getData().rotate(rotation));
    }

    public MaterialList getRequiredMaterials(BuildContext context, @Nullable RayTraceResult target) {
        return getData().getRequiredItems(context, target, getPos());
    }

    /**
     * Serializes the data contained by this {@link PlacementTarget}. The persisted flag is used as an hint, whether non-persistent formats (Registry-id's) may be used to
     * reduce the size of the resulting {@link CompoundNBT}.
     * @param persisted Whether or not this should be created as an persisted save.
     * @return The serialized form of this {@code PlacementTarget} as an {@link CompoundNBT}.
     * @see BlockData#serialize(boolean)
     */
    public CompoundNBT serialize(boolean persisted) {
        CompoundNBT compound = new CompoundNBT();
        compound.put(NBTKeys.KEY_DATA, data.serialize(persisted));
        compound.put(NBTKeys.KEY_POS, NBTUtil.writeBlockPos(pos));
        return compound;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pos", pos)
                .add("data", data)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof PlacementTarget)) return false;

        PlacementTarget that = (PlacementTarget) o;

        if (! getPos().equals(that.getPos())) return false;
        return getData().equals(that.getData());
    }

    @Override
    public int hashCode() {
        int result = getPos().hashCode();
        result = 31 * result + getData().hashCode();
        return result;
    }
}
