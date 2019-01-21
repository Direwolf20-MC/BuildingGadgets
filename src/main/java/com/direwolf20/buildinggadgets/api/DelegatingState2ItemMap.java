package com.direwolf20.buildinggadgets.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

public final class DelegatingState2ItemMap {
    private final BlockState2ItemMap theMap;

    public DelegatingState2ItemMap(BlockState2ItemMap theMap) {
        this.theMap = theMap;
    }

    public UniqueItem getItemForState(IBlockState state) {
        return theMap.getItemForState(state);
    }

    public short getSlot(IBlockState mapState) {
        return theMap.getSlot(mapState);
    }

    public IBlockState getStateFromSlot(short slot) {
        return theMap.getStateFromSlot(slot);
    }

    public BlockState2ItemMap getCopy() {
        return new BlockState2ItemMap(theMap.getShortStateMap(), theMap.getStateItemMap());
    }

    public void writeToNBT(NBTTagCompound compound) {
        theMap.writeToNBT(compound);
    }
}
