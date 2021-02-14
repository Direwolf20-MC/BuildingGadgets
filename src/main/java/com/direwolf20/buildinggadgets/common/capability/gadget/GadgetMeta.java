package com.direwolf20.buildinggadgets.common.capability.gadget;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.Modes;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class GadgetMeta {
    private final ItemStack gadget;

    private IMode mode;
    private BlockState selectedBlock;
    private BlockPos anchor;
    private int range = 1;
    private boolean fuzzy = false;
    private boolean connectedArea = false;
    private boolean fluidTrace = false;
    private boolean placeOnTop = true;
    private boolean showOverlay = true;

    public GadgetMeta(ItemStack stack) {
        this.gadget = stack;
    }

    public BlockState getBlockState() {
        return selectedBlock;
    }

    public void setBlockState(BlockState state) {
        this.selectedBlock = state;
    }

    public boolean canFluidTrace() {
        return fluidTrace;
    }

    public void setFluidTrace(boolean fluidTrace) {
        this.fluidTrace = fluidTrace;
    }

    public IMode getMode() {
        return this.mode;
    }

    public void setMode(IMode mode) {
        this.mode = mode;
    }

    /**
     * Wrap this with an optional as most logic related to this is conditional
     */
    public Optional<BlockPos> getAnchor() {
        if (this.anchor == null) {
            return Optional.empty();
        }

        return Optional.of(this.anchor);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setAnchor(BlockPos anchor) {
        this.anchor = anchor;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public boolean isConnectedArea() {
        return connectedArea;
    }

    public void setConnectedArea(boolean connectedArea) {
        this.connectedArea = connectedArea;
    }

    public boolean isPlaceOnTop() {
        return placeOnTop;
    }

    public void setPlaceOnTop(boolean placeOnTop) {
        this.placeOnTop = placeOnTop;
    }

    public boolean isShowOverlay() {
        return showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }

    public void deserialize(CompoundNBT compound) {
        this.selectedBlock = NBTUtil.readBlockState(compound.getCompound("selected"));
        if (compound.contains("anchor")) {
            this.anchor = NBTUtil.readBlockPos(compound.getCompound("anchor"));
        }
        this.range = compound.getInt("range");
        this.fluidTrace = compound.getBoolean("fluid_trace");
        this.fuzzy = compound.getBoolean("fuzzy");
        this.connectedArea = compound.getBoolean("connected");
        this.placeOnTop = compound.getBoolean("on_top");
        this.showOverlay = compound.getBoolean("show_overlay");
        this.mode = Modes.getFromName(((AbstractGadget) this.gadget.getItem()).getModes(), new ResourceLocation(compound.getString("mode")));
    }

    public CompoundNBT serialize() {
        CompoundNBT compound = new CompoundNBT();

        if (this.selectedBlock != null) {
            compound.put("selected", NBTUtil.writeBlockState(this.selectedBlock));
        }

        compound.putBoolean("fluid_trace", this.fluidTrace);
        compound.putString("mode", mode.getRegistryName().toString());
        if (this.anchor != null) {
            compound.put("anchor", NBTUtil.writeBlockPos(this.anchor));
        }

        compound.putInt("range", this.range);
        compound.putBoolean("fuzzy", this.fuzzy);
        compound.putBoolean("connected", this.connectedArea);
        compound.putBoolean("on_top", this.placeOnTop);
        compound.putBoolean("show_overlay", this.showOverlay);

        return compound;
    }
}
