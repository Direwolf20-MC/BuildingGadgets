package com.direwolf20.buildinggadgets.building.placement;

import com.direwolf20.buildinggadgets.building.IBlockProvider;
import com.direwolf20.buildinggadgets.building.TranslationWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class SingleTypeProvider implements IBlockProvider {

    private final IBlockState state;

    public SingleTypeProvider(IBlockState state) {
        this.state = state;
    }

    @Override
    public TranslationWrapper translate(BlockPos origin) {
        return new TranslationWrapper(this, origin);
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
