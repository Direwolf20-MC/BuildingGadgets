package com.direwolf20.buildinggadgets.api.building.tilesupport;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;
import java.util.Objects;

public class NBTTileEntityData implements ITileEntityData {
    private final CompoundNBT nbt;
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
    public MaterialList getRequiredItems(IBuildContext context, BlockState state, @Nullable RayTraceResult target, @Nullable BlockPos pos) {
        if (requiredMaterials != null)
            return requiredMaterials;
        return ITileEntityData.super.getRequiredItems(context, state, target, pos);
    }

    @Override
    public boolean placeIn(IBuildContext context, BlockState state, BlockPos position) {
        BuildingGadgetsAPI.LOG.trace("Placing {} with Tile NBT at {}.", state, position);
        context.getWorld().setBlockState(position, state, 0);
        TileEntity te = context.getWorld().getTileEntity(position);
        if (te != null) {
            try {
                te.read(getNBTModifiable());
            } catch (Exception e) {
                BuildingGadgetsAPI.LOG.debug("Failed to apply Tile NBT Data to {} at {} in Context {}", state, position, context);
                BuildingGadgetsAPI.LOG.debug(e);
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

}
