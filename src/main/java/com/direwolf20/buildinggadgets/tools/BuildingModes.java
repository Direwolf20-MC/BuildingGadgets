package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.items.BuildingTool;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import static net.minecraft.util.EnumFacing.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BuildingModes {
    private static boolean isReplaceable(World world, BlockPos pos, IBlockState setBlock) {
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos) || !setBlock.getBlock().canPlaceBlockAt(world, pos)) {
            return false;
        }
        return true;
    }

    public static ArrayList<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, int range, BuildingTool.Mode mode, IBlockState setBlock) {

        ArrayList<BlockPos> coordinates = new ArrayList<BlockPos>();
        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        BlockPos pos = startBlock;
        int bound = (range - 1) / 2;
        EnumFacing playerFacing = player.getHorizontalFacing();

        //***************************************************
        //Build to me
        //***************************************************
        if (mode == BuildingTool.Mode.BUILD_TO_ME) {
            int dist;
            if(sideHit == UP || sideHit == DOWN)
                dist = Math.abs(startBlock.getY() - playerPos.getY());
            else if(sideHit == EAST || sideHit == WEST)
                dist = Math.abs(startBlock.getX() - playerPos.getX());
            else
                dist = Math.abs(startBlock.getZ() - playerPos.getZ());

            dist -= 2;//don't pick player and startBlock pos;
            for(BlockPos p = startBlock.offset(sideHit); dist > 0; dist--, p = p.offset(sideHit))
                if(isReplaceable(world, p, setBlock))
                    coordinates.add(p);
            
        }
        //***************************************************
        //VerticalWall
        //***************************************************
        else if (mode == BuildingTool.Mode.VERTICAL_WALL) {
            BlockPos start = startBlock;
            if(sideHit == UP)
                start = start.offset(UP);
            else if(sideHit == DOWN)
                start = start.offset(DOWN, range);
            else 
                start = start.offset(DOWN, bound);
            
            for(int x = -bound; x <= bound; x++) {
                BlockPos p = start.offset(playerFacing.rotateY(), x);
                for(int y = 0; y < range; y++, p = p.offset(UP)) {
                    if(isReplaceable(world, p, setBlock))
                        coordinates.add(p);
                }
            }
        }
        //***************************************************
        //VerticalColumn
        //***************************************************
        else if (mode == BuildingTool.Mode.VERTICAL_COLUMN) {
            BlockPos p;
            if(sideHit == UP)
                p = startBlock.offset(UP);
            else if(sideHit == DOWN)
                p = startBlock.offset(DOWN, range);
            else p = startBlock.offset(DOWN, bound);
            for(int y = 0; y < range; y++, p = p.offset(UP)) {
                if(isReplaceable(world, p, setBlock))
                    coordinates.add(p);
            }
        }
        //***************************************************
        //HorizontalColumn
        //***************************************************
        else if (mode == BuildingTool.Mode.HORIZONTAL_COLUMN) {
            if (sideHit == UP || sideHit == DOWN)
                sideHit = playerFacing;
            else sideHit = sideHit.getOpposite();

            for(BlockPos p = startBlock.offset(sideHit); range > 0; range--, p = p.offset(sideHit))
                if(isReplaceable(world, p, setBlock))
                    coordinates.add(p);
        }
        //***************************************************
        //HorizontalWall
        //***************************************************
        else if (mode == BuildingTool.Mode.HORIZONTAL_WALL) {
            BlockPos start = startBlock;
            if(sideHit == UP || sideHit == DOWN)
                start = start.offset(sideHit.getOpposite(), bound);
            else start = start.offset(sideHit);
            
            for(int x = -bound; x <= bound; x++) {
                BlockPos p;
                if(sideHit == UP || sideHit == DOWN) {
                    for(int z = -bound; z <= bound; z++) {
                        p = start.add(x, 0, z);
                        if(isReplaceable(world, p, setBlock))
                            coordinates.add(p);
                    }
                }
                else {
                    p = start.offset(sideHit.rotateY(), x);
                    for(int a = 0; a < range; a++, p = p.offset(sideHit)) {
                        if(isReplaceable(world, p, setBlock))
                            coordinates.add(p);
                    }
                }
            }
        }
        //***************************************************
        //Stairs
        //***************************************************
        else if (mode == BuildingTool.Mode.STAIRS) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() - z);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - 1);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() + z);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + 1);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() - x + 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY() - x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() + x - 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //Checkerboard
        //***************************************************
        else if (mode == BuildingTool.Mode.CHECKERBOARD) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + 1, startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        return coordinates;
    }

    public static ArrayList<BlockPos> sortByDistance(ArrayList<BlockPos> unSortedList, EntityPlayer player) {
        ArrayList<BlockPos> sortedList = new ArrayList<BlockPos>();
        Map<Double, BlockPos> rangeMap = new HashMap<Double, BlockPos>();
        Double distances[] = new Double[unSortedList.size()];
        Double distance;
        double x = player.posX;
        double y = player.posY + player.getEyeHeight();
        double z = player.posZ;
        int i = 0;
        for (BlockPos pos : unSortedList) {
            distance = pos.distanceSqToCenter(x, y, z);
            rangeMap.put(distance, pos);
            distances[i] = distance;
            i++;
        }
        Arrays.sort(distances);
        //ArrayUtils.reverse(distances);

        for (Double dist : distances) {
            //System.out.println(dist);
            sortedList.add(rangeMap.get(dist));
        }
        //System.out.println(unSortedList);
        //System.out.println(sortedList);
        return sortedList;
    }
}
