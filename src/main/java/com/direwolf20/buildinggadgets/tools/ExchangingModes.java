package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class ExchangingModes {
    private static boolean isReplaceable(World world, BlockPos pos, IBlockState currentBlock, IBlockState setBlock, EntityPlayer player, boolean fuzzyMode) {
        IBlockState worldBlockState = world.getBlockState(pos);
        if (worldBlockState != currentBlock && !fuzzyMode) {return false;}
        if (worldBlockState == ModBlocks.effectBlock.getDefaultState()) {return false;}
        if (worldBlockState == setBlock)  {return false;}
        if (world.getTileEntity(pos) != null) {return false;}
        if (worldBlockState.getBlock().getBlockHardness(worldBlockState, world, pos) < 0) {return false;}
        /*if (world.getBlockState(pos) != currentBlock || world.getBlockState(pos) == ModBlocks.effectBlock.getDefaultState() || world.getBlockState(pos) == setBlock || world.getTileEntity(pos) != null || currentBlock.getBlock().getBlockHardness(currentBlock, world, pos) < 0) {
            return false;
        }*/
        return true;
    }

    public static ArrayList<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, int range, ExchangerTool.Mode mode, IBlockState setBlock, boolean fuzzyMode) {

        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        BlockPos pos = startBlock;
        int bound = (range - 1) / 2;
        EnumFacing playerFacing = player.getHorizontalFacing();
        int boundX, boundZ;
        if (playerFacing == EnumFacing.SOUTH || playerFacing == EnumFacing.NORTH) {
            boundX = bound;
            boundZ = 0;
        } else {
            boundX = 0;
            boundZ = bound;
        }

        IBlockState currentBlock = world.getBlockState(startBlock);

        //***************************************************
        //VerticalWall
        //***************************************************
        if (mode == ExchangerTool.Mode.WALL) {
            EnumFacing growFrom = 
                sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN ? EnumFacing.SOUTH : EnumFacing.DOWN;
            EnumFacing side = growFrom == EnumFacing.SOUTH ? EnumFacing.EAST : sideHit.rotateY();

            for(int y = -bound; y <= bound; y++) {
                for(int x = -bound; x <= bound; x++) {
                    BlockPos p = startBlock.offset(growFrom, y).offset(side, x);

                    if (isReplaceable(world, p, currentBlock, setBlock, player, fuzzyMode))
                        coordinates.add(p);
                }
            }
        }
        //***************************************************
        //VerticalColumn
        //***************************************************
        if (mode == ExchangerTool.Mode.VERTICAL_COLUMN) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                for (int x = boundZ * -1; x <= boundZ; x++) {
                    for (int z = boundX * -1; z <= boundX; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int y = bound; y >= bound * -1; y--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalColumn
        //***************************************************
        if (mode == ExchangerTool.Mode.HORIZONTAL_COLUMN) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                for (int x = boundX * -1; x <= boundX; x++) {
                    for (int z = boundZ * -1; z <= boundZ; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.NORTH || sideHit == EnumFacing.SOUTH) {
                for (int x = bound * -1; x <= bound; x++) {
                    pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST || sideHit == EnumFacing.WEST) {
                for (int z = bound * -1; z <= bound; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //TorchPlacer
        //***************************************************
        else if (mode == ExchangerTool.Mode.CHECKERBOARD) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, player, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        return coordinates;
    }
}
