package com.direwolf20.buildinggadgets.common.capability;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class GadgetMetaCapability {
    private final ItemStack gadget;

    private BlockState selectedBlock;

    public GadgetMetaCapability(ItemStack stack) {
        this.gadget = stack;
    }

    public void setSelectedBlock(BlockState selectedBlock) {
        this.selectedBlock = selectedBlock;
    }

    public BlockState getSelectedBlock() {
        return selectedBlock;
    }

    public CompoundNBT serialize() {
        CompoundNBT compound = new CompoundNBT();
        if (this.selectedBlock != null) {
            compound.put("selected", NBTUtil.writeBlockState(this.selectedBlock));
        }
        return compound;
    }

    public void deserialize(CompoundNBT compound) {
        if (compound.contains("selected")) {
            this.selectedBlock = NBTUtil.readBlockState(compound.getCompound("selected"));
        }
    }
}
