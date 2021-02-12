package com.direwolf20.buildinggadgets.common.capability.gadget;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class GadgetMeta {
    private BlockState selectedBlock;
    private final ItemStack gadget;

    public GadgetMeta(ItemStack stack) {
        this.gadget = stack;
    }

    public BlockState getBlockState() {
        return selectedBlock;
    }

    public void setBlockState(BlockState state) {
        this.selectedBlock = state;
    }

    public void deserialize(CompoundNBT compound) {
        if (compound.contains("selected")) {
            this.selectedBlock = NBTUtil.readBlockState(compound.getCompound("selected"));
        }
    }

    public CompoundNBT serialize() {
        CompoundNBT compoundNBT = new CompoundNBT();
        if (this.selectedBlock != null) {
            compoundNBT.put("selected", NBTUtil.writeBlockState(this.selectedBlock));
        }
        return compoundNBT;
    }
}
