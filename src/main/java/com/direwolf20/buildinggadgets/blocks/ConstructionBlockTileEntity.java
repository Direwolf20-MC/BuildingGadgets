package com.direwolf20.buildinggadgets.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
    }

    public boolean setBlockState(IBlockState state) {
        blockState = state;
        System.out.println(state);
        markDirty();
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (blockState != null) {
            NBTTagCompound blockStateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(blockStateTag, blockState);
            compound.setTag("blockState", blockStateTag);
        }
        return compound;
    }
}
