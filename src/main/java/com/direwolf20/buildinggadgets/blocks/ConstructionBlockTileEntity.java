package com.direwolf20.buildinggadgets.blocks;

import com.direwolf20.buildinggadgets.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;

    public boolean setBlockState(IBlockState state) {
        blockState = state;
        System.out.println(state);
        markDirtyClient();
        return true;
    }

    public IBlockState getBlockState() {
        if (blockState == null || blockState == Blocks.AIR.getDefaultState()) {
            return ModBlocks.constructionBlock.getDefaultState();
        }
        return blockState;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
    }

    public void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        //@Todo make this look more like MCJty's
        IBlockState oldMimicBlock = getBlockState();
        super.onDataPacket(net, packet);
        if (world.isRemote) {
            // If needed send a render update.
            if (getBlockState() != oldMimicBlock) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (blockState != null) {
            NBTTagCompound blockStateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(blockStateTag, blockState);
            compound.setTag("blockState", blockStateTag);
        }
        return compound;
    }
}
