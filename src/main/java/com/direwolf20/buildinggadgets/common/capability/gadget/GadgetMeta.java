package com.direwolf20.buildinggadgets.common.capability.gadget;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class GadgetMeta implements IGadgetMeta {
    private BlockState selectedBlock;
    private final ItemStack gadget;

    public GadgetMeta(ItemStack stack) {
        this.gadget = stack;
    }

    @Override
    public BlockState getBlockState() {
        return selectedBlock;
    }

    @Override
    public void setBlockState(BlockState state) {
        this.selectedBlock = state;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT compound = new CompoundNBT();
        if (this.selectedBlock != null) {
            compound.put("selected", NBTUtil.writeBlockState(this.selectedBlock));
        }
        return compound;
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        if (compound.contains("selected")) {
            this.selectedBlock = NBTUtil.readBlockState(compound.getCompound("selected"));
        }
    }
}
