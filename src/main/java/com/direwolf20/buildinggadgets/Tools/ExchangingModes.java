package com.direwolf20.buildinggadgets.Tools;

import com.direwolf20.buildinggadgets.Items.ExchangerTool;
import com.direwolf20.buildinggadgets.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class ExchangingModes {
    private static boolean isReplaceable(World world, BlockPos pos, IBlockState currentBlock, IBlockState setBlock) {
        //System.out.println(currentBlock);
        //System.out.println(pos+":"+world.getBlockState(pos).getBlock().getExtendedState(world.getBlockState(pos), world, pos));
        if (world.getBlockState(pos) != currentBlock || world.getBlockState(pos) == ModBlocks.effectBlock.getDefaultState() || world.getBlockState(pos) == setBlock || world.getTileEntity(pos) != null) {
            return false;
        }
        return true;
    }

    public static ArrayList<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, int range, ExchangerTool.toolModes mode, IBlockState setBlock) {

        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        BlockPos playerPos = new BlockPos (Math.floor(player.posX),Math.floor(player.posY),Math.floor(player.posZ));
        BlockPos pos = startBlock;
        int bound = (range-1)/2;
        EnumFacing playerFacing = player.getHorizontalFacing();
        int boundX, boundZ;
        if (playerFacing == EnumFacing.SOUTH || playerFacing == EnumFacing.NORTH) {
            boundX = bound;
            boundZ = 0;
        }
        else {
            boundX = 0;
            boundZ = bound;
        }
        IBlockState currentBlock = world.getBlockState(startBlock);
        //IBlockState currentBlock = world.getBlockState(startBlock).getBlock().getExtendedState(world.getBlockState(startBlock), world, pos);

        //***************************************************
        //VerticalWall
        //***************************************************
        if (mode == ExchangerTool.toolModes.Wall) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN ) {
                for (int x = bound*-1; x <= bound; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ()+z);
                        if (isReplaceable(world,pos,currentBlock,setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
            else {
                for (int y = bound; y >= bound*-1; y--) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos,currentBlock,setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
        }
        return coordinates;
    }
}
