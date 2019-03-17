package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class BlockData {
    private static final String KEY_STATE = "state";
    private static final String KEY_SERIALIZER = "serializer";
    private static final String KEY_DATA = "data";

    public static BlockData deserialize(NBTTagCompound compound, boolean persisted) {
        return null; //TODO requires ForgeReg to implement
    }

    private final IBlockState state;
    private final ITileEntityData tileData;

    public BlockData(IBlockState state, ITileEntityData tileData) {
        this.state = Objects.requireNonNull(state);
        this.tileData = Objects.requireNonNull(tileData);
    }

    public IBlockState getState() {
        return state;
    }

    public ITileEntityData getTileData() {
        return tileData;
    }

    public boolean placeIn(IBuildContext context, BlockPos pos) {
        return tileData.allowPlacement(context, state, pos) && tileData.placeIn(context, state, pos);
    }

    public NBTTagCompound serialize(boolean persisted) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(KEY_STATE, NBTUtil.writeBlockState(state));
        //TODO requires ForgeReg to implement properly
        tag.setTag(KEY_DATA, tileData.getSerializer().serialize(tileData, persisted));
        return tag;
    }
}
