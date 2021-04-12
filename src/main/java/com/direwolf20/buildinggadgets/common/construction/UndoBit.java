package com.direwolf20.buildinggadgets.common.construction;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * An UndoBit identifies specific data about a action ("bit") which can be used to
 * check and remove any action performed by a gadget.
 */
public class UndoBit {
    private BlockPos pos;
    private BlockState state;
    private RegistryKey<World> dimension;

    public UndoBit(BlockPos pos, BlockState state, RegistryKey<World> dimension) {
        this.pos = pos;
        this.state = state;
        this.dimension = dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @Nullable
    public static UndoBit deserialize(CompoundNBT compound) {
        // Fail if we don't have a valid compound
        if (!compound.contains("pos") || !compound.contains("state") || !compound.contains("dimension"))
            return null;

        return new UndoBit(
                NBTUtil.readBlockPos(compound.getCompound("pos")),
                NBTUtil.readBlockState(compound.getCompound("state")),
                RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(compound.getString("dimension")))
        );
    }

    public CompoundNBT serialize() {
        RegistryKey<World> dimensionName = this.dimension;
        if (dimensionName == null) {
            BuildingGadgets.LOGGER.fatal("Current dimension does not have a registry name!");
            return new CompoundNBT();
        }

        CompoundNBT compound = new CompoundNBT();
        compound.put("pos", NBTUtil.writeBlockPos(this.pos));
        compound.put("state", NBTUtil.writeBlockState(this.state));
        compound.putString("dimension", dimensionName.getLocation().toString());
        return compound;
    }
}
