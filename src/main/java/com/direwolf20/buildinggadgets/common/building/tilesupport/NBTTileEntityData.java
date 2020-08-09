package com.direwolf20.buildinggadgets.common.building.tilesupport;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.template.SerialisationSupport;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class NBTTileEntityData implements ITileEntityData {
    public static NBTTileEntityData ofTile(TileEntity te) {
        CompoundNBT nbt = new CompoundNBT();
        te.write(nbt);
        return new NBTTileEntityData(nbt);
    }
    @Nonnull
    private final CompoundNBT nbt;
    @Nullable
    private final MaterialList requiredMaterials;

    public NBTTileEntityData(CompoundNBT nbt, @Nullable MaterialList requiredMaterials) {
        this.nbt = Objects.requireNonNull(nbt);
        this.requiredMaterials = requiredMaterials;
    }

    public NBTTileEntityData(CompoundNBT nbt) {
        this(nbt, null);
    }

    @Override
    public ITileDataSerializer getSerializer() {
        return SerialisationSupport.nbtTileDataSerializer();
    }

    @Override
    public MaterialList getRequiredItems(BuildContext context, BlockState state, @Nullable RayTraceResult target, @Nullable BlockPos pos) {
        if (requiredMaterials != null)
            return requiredMaterials;
        return ITileEntityData.super.getRequiredItems(context, state, target, pos);
    }

    @Override
    public boolean placeIn(BuildContext context, BlockState state, BlockPos position) {
        BuildingGadgets.LOG.trace("Placing {} with Tile NBT at {}.", state, position);
        context.getWorld().setBlockState(position, state, 0);
        TileEntity te = context.getWorld().getTileEntity(position);
        if (te != null) {
            try {
                te.deserializeNBT(getNBTModifiable());
            } catch (Exception e) {
                BuildingGadgets.LOG.debug("Failed to apply Tile NBT Data to {} at {} in Context {}", state, position, context, e);
            }
        }
        return true;
    }

    public CompoundNBT getNBT() {
        return nbt.copy();
    }

    @Nullable
    public MaterialList getRequiredMaterials() {
        return requiredMaterials;
    }

    protected CompoundNBT getNBTModifiable() {
        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof NBTTileEntityData)) return false;

        NBTTileEntityData that = (NBTTileEntityData) o;

        if (! nbt.equals(that.nbt)) return false;
        return getRequiredMaterials() != null ? getRequiredMaterials().equals(that.getRequiredMaterials()) : that.getRequiredMaterials() == null;
    }

    @Override
    public int hashCode() {
        int result = nbt.hashCode();
        result = 31 * result + (getRequiredMaterials() != null ? getRequiredMaterials().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nbt", nbt)
                .add("requiredMaterials", requiredMaterials)
                .toString();
    }
}
