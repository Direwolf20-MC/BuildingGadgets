package com.direwolf20.buildinggadgets.api.abstraction;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.util.RegistryUtils;
import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * Representation of the data one block can hold, in the form of an {@link IBlockState} and an instance of {@link ITileEntityData}.
 * This calls offers serialisation facilities as well as delegating placement through to the {@link ITileEntityData}.
 * <p>
 * Notice that this class is immutable as long as the {@link ITileEntityData} instance is immutable.
 */
public final class BlockData {
    private static final String KEY_STATE = "state";
    private static final String KEY_SERIALIZER = "serializer";
    private static final String KEY_DATA = "data";

    /**
     * @param tag The {@link NBTTagCompound} representing the serialized block data.
     * @param persisted Whether or not the {@link NBTTagCompound} was created using an persisted save.
     * @return A new instance of {@code BlockData} as represented by the {@link NBTTagCompound}.
     * @throws IllegalArgumentException if the persisted flag does not match how the tag was created.
     * @throws NullPointerException if an unknown serializer is referenced.
     */
    public static BlockData deserialize(NBTTagCompound tag, boolean persisted) {
        IBlockState state = NBTUtil.readBlockState(tag.getCompound(KEY_STATE));
        ITileDataSerializer serializer;
        try {
            if (persisted)
                serializer = RegistryUtils
                        .getFromString(Registries.getTileDataSerializers(), tag.getString(KEY_SERIALIZER));
            else
                serializer = RegistryUtils.getById(Registries.getTileDataSerializers(), tag.getInt(KEY_SERIALIZER));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not retrieve serializer with persisted=" + persisted + "!", e);
        }
        Preconditions.checkArgument(serializer != null);
        ITileEntityData data = serializer.deserialize(tag.getCompound(KEY_DATA), persisted);
        return new BlockData(state, data);
    }

    private final IBlockState state;
    private final ITileEntityData tileData;

    /**
     * Creates a new {@code BlockData} with the specified data.
     * @param state The {@link IBlockState} of the resulting data.
     * @param tileData The {@link ITileEntityData} of the resulting data.
     * @throws NullPointerException if state or tileData are null.
     */
    public BlockData(IBlockState state, ITileEntityData tileData) {
        this.state = Objects.requireNonNull(state);
        this.tileData = Objects.requireNonNull(tileData);
    }

    /**
     * @return The {@link IBlockState} contained by this {@code BlockData}
     */
    public IBlockState getState() {
        return state;
    }

    /**
     * @return The {@link ITileEntityData} contained by this {@code BlockState}.
     */
    public ITileEntityData getTileData() {
        return tileData;
    }

    /**
     * @param context The {@link IBuildContext} in which to perform the placement.
     * @param pos The {@link BlockPos} at which to perform the placement.
     * @return whether or not the {@link ITileEntityData} reported that placement was performed.
     */
    public boolean placeIn(IBuildContext context, BlockPos pos) {
        return tileData.allowPlacement(context, state, pos) && tileData.placeIn(context, state, pos);
    }

    /**
     * Serializes this {@code BlockData} to NBT. If persisted is false, registry id's will be used instead of registry-names, for serialisation.
     * @param persisted Whether or not this should be written as a persisted save.
     * @return The serialized form of this {@code BlockData}.
     */
    public NBTTagCompound serialize(boolean persisted) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(KEY_STATE, NBTUtil.writeBlockState(state));
        if (persisted)
            tag.setString(KEY_SERIALIZER, tileData.getSerializer().getRegistryName().toString());
        else
            tag.setInt(KEY_SERIALIZER, RegistryUtils
                    .getId(Registries.getTileDataSerializers(), tileData.getSerializer()));
        tag.setTag(KEY_DATA, tileData.getSerializer().serialize(tileData, persisted));
        return tag;
    }
}
