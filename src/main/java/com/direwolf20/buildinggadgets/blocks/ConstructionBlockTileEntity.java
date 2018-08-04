package com.direwolf20.buildinggadgets.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;

public class ConstructionBlockTileEntity extends TileEntity {
    private IBlockState blockState;
    private IBlockState actualBlockState;

    public boolean setBlockState(IBlockState state) {
        blockState = state;
        System.out.println(state);
        markDirtyClient();
        return true;
    }

    public boolean setActualBlockState(IBlockState state) {
        actualBlockState = state;
        System.out.println(state);
        markDirtyClient();
        return true;
    }

    public IBlockState getBlockState() {
        if (blockState == null || blockState == Blocks.AIR.getDefaultState()) {
            //return Blocks.COBBLESTONE.getDefaultState();
            return null;
        }
        return blockState;
    }

    public IBlockState getActualBlockState() {
        if (actualBlockState == null || actualBlockState == Blocks.AIR.getDefaultState()) {
            return null;
        }
        return actualBlockState;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        blockState = NBTUtil.readBlockState(compound.getCompoundTag("blockState"));
        actualBlockState = NBTUtil.readBlockState(compound.getCompoundTag("actualBlockState"));
        markDirtyClient();
    }

    private static Method relightBlock = ReflectionHelper.findMethod(Chunk.class, "relightBlock", "func_76615_h", int.class, int.class, int.class);

    private static Method getRelightBlockMethod() {
        try {
            Method ret = Chunk.class.getDeclaredMethod("relightBlock", int.class, int.class, int.class);
            ret.setAccessible(true);
            return ret;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /*private static Method getPropegateMethod() {
        try {
            Method ret = Chunk.class.getDeclaredMethod("propagateSkylightOcclusion", int.class, int.class);
            ret.setAccessible(true);
            return ret;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }*/

    private static final Method relightBlockMethod = getRelightBlockMethod();
    //private static final Method propegateMethod = getPropegateMethod();

    public void markDirtyClient() {

        markDirty();
        if (getWorld() != null) {
            BlockPos pos = getPos();
            IBlockState state = getWorld().getBlockState(getPos());
            //world.checkLight(getPos());
            //updateLighting();
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    public void updateLighting() {
        if (getWorld() != null) {
            try {
                System.out.println("Doing Lighting");
                relightBlock.invoke(world.getChunkFromBlockCoords(getPos()), pos.getX() & 15, pos.getY() + 1, pos.getZ() & 15);
                world.checkLight(getPos());
                //relightBlockMethod.invoke(world.getChunkFromBlockCoords(getPos()), pos.getX() & 15, pos.getY(), pos.getZ() & 15);
                //propegateMethod.invoke(world.getChunkFromBlockCoords(getPos()), pos.getX() & 15, pos.getZ() & 15);
                //IBlockState state = getWorld().getBlockState(getPos());
                //getWorld().notifyBlockUpdate(getPos().down(), state, state, 3);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

        }
    }


    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        writeToNBT(updateTag);
        return updateTag;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = getBlockState();
        NBTTagCompound tagCompound = packet.getNbtCompound();
        super.onDataPacket(net, packet);
        readFromNBT(tagCompound);
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
            NBTTagCompound actualBlockStateTag = new NBTTagCompound();
            if (blockState != null) {
                NBTUtil.writeBlockState(blockStateTag, blockState);
                compound.setTag("blockState", blockStateTag);
            }
            if (actualBlockState != null) {
                NBTUtil.writeBlockState(actualBlockStateTag, actualBlockState);
                compound.setTag("actualBlockState", actualBlockStateTag);
            }
        }
        return compound;
    }
}
