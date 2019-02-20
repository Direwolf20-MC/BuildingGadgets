package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.placement.IBlockProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class SingleTypeProvider implements IBlockProvider {

    private final IBlockState state;

    public SingleTypeProvider(IBlockState state) {
        this.state = state;
    }

    @Override
    public IBlockState at(BlockPos pos) {
        return state;
    }

    public IBlockState getBlockState() {
        return state;
    }

    public NBTTagCompound serializeNBT() {
        return NBTUtil.writeBlockState(new NBTTagCompound(), state);
    }

    public SingleTypeProvider deserializeNBT(NBTTagCompound tag) {
        return new SingleTypeProvider(NBTUtil.readBlockState(tag));
    }

}
