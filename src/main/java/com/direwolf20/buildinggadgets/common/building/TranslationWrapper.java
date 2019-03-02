package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Wraps an {@link IBlockProvider} such that all access to the handle will be translated by the given amount as the
 * BlockPos passed into the constructor.
 */
public final class TranslationWrapper implements IBlockProvider {

    private final IBlockProvider handle;
    /**
     * Translation done by the current wrapper.
     */
    private final BlockPos translation;
    /**
     * Translation done by the current wrapper and its handle.
     */
    private final BlockPos accumulatedTranslation;

    /**
     * @implNote you cannot create a object and pass itself as a parameter of the constructor, so we are safe from looping
     */
    public TranslationWrapper(IBlockProvider handle, BlockPos origin) {
        this.handle = handle;
        this.translation = origin;
        this.accumulatedTranslation = handle.getTranslation().add(origin);
    }

    @Override
    public IBlockProvider translate(BlockPos origin) {
        //Since handle is the same, just adding the two translation together would be sufficient
        return new TranslationWrapper(handle, translation.add(origin));
    }

    /**
     * @return all translation done with the wrapper and its underlying block provider.
     */
    @Override
    public BlockPos getTranslation() {
        return accumulatedTranslation;
    }

    /**
     * @return the translation applied by the current wrapper
     */
    public BlockPos getAppliedTranslation() {
        return translation;
    }

    /**
     * The underlying block provider that was wrapped in the constructor.
     */
    public IBlockProvider getHandle() {
        return handle;
    }

    /**
     * Redirects the call to the wrapped IBlockProvider.
     */
    @Override
    public IBlockState at(BlockPos pos) {
        return handle.at(pos.add(translation));
    }

    /**
     * @see #serialize(NBTTagCompound)
     */
    public NBTTagCompound serialize() {
        return serialize(new NBTTagCompound());
    }

    /**
     * @return tag itself when serialization fails
     */
    @SuppressWarnings("unchecked")
    //Safe raw type usage since INBTSerializable<T extends NBTBase> and NBTTagCompound is subclass of it
    public NBTTagCompound serialize(NBTTagCompound tag) {
        if (handle instanceof INBTSerializable) {
            tag.merge(((INBTSerializable<NBTTagCompound>) handle).serializeNBT());
            return tag;
        }
        if (handle instanceof SingleTypeProvider) {
            ((SingleTypeProvider) handle).deserialize(tag);
        }
        return tag;
    }

    /**
     * @return this when deserialization fails
     */
    @SuppressWarnings("unchecked")
    //Safe raw type usage since INBTSerializable<T extends NBTBase> and NBTTagCompound is subclass of it
    public TranslationWrapper deserialize(NBTTagCompound tag) {
        if (handle instanceof INBTSerializable) {
            //This operation mutates the handle
            ((INBTSerializable<NBTTagCompound>) handle).deserializeNBT(tag);
            return this;
        }
        if (handle instanceof SingleTypeProvider) {
            return new TranslationWrapper(((SingleTypeProvider) handle).deserialize(tag), translation);
        }
        return this;
    }

}
