package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Used to store a single Blocks Position along with it's state
 * It's important that we also remember if we're storing a construction
 * block as this will be important to know for many of our tools.
 *
 * This class also comes with a handy method to allow us to convert our data
 * into a NBTTagCompound and back out of a NBTTagCompound making this ideal
 * for any code that uses this data to store to the world save.
 */
public class BlockPosState {
    private static final String NBT_BLOCK_POS = "block_pos";
    private static final String NBT_BLOCK_STATE = "block_state";
    private static final String NBT_BLOCK_PASTE = "block_is_paste";

    private BlockPos pos;
    private IBlockState state;
    private boolean isPaste;

    public BlockPosState(BlockPos pos, IBlockState state, boolean isPaste) {
        this.pos = pos;
        this.state = state;
        this.isPaste = isPaste;
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }

    public boolean isPaste() {
        return isPaste;
    }

    /**
     * Convert our data to an NBTTagCompound
     *
     * @return NBTTagCompound a compound with a position, state and paste
     */
    public NBTTagCompound toCompound() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound stateCompound = new NBTTagCompound();
        NBTTagCompound posCompound = NBTUtil.createPosTag(this.pos);

        NBTUtil.writeBlockState(stateCompound, this.state);

        compound.setTag(NBT_BLOCK_POS, posCompound);
        compound.setTag(NBT_BLOCK_STATE, stateCompound);
        compound.setBoolean(NBT_BLOCK_PASTE, this.isPaste);

        return compound;
    }

    /**
     * Convert our NBTTagCompound from toCompound back out to a new
     * BlockPosState.
     */
    @Nullable
    public static BlockPosState fromCompound(NBTTagCompound compound) {
        if( !compound.hasKey(NBT_BLOCK_POS) )
            return null;

        return new BlockPosState(
                NBTUtil.getPosFromTag( compound.getCompoundTag(NBT_BLOCK_POS) ),
                NBTUtil.readBlockState( compound.getCompoundTag(NBT_BLOCK_STATE) ),
                compound.getBoolean(NBT_BLOCK_PASTE)
        );
    }
}
