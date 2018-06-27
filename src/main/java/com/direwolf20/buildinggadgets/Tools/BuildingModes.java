package com.direwolf20.buildinggadgets.Tools;

import com.direwolf20.buildinggadgets.Items.BuildingTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class BuildingModes {
    private static boolean isReplaceable(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock().isReplaceable(world,pos)) {return true;}
        return false;
    }

    public static Set<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, int range, BuildingTool.toolModes mode) {

        Set<BlockPos> coordinates = new HashSet<>();
        BlockPos playerPos = new BlockPos (Math.floor(player.posX),Math.floor(player.posY),Math.floor(player.posZ));
        //System.out.println(player.posX);
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
        //***************************************************
        //Build to me
        //***************************************************
        if (mode == BuildingTool.toolModes.BuildToMe) {
            if (sideHit == EnumFacing.SOUTH) {
                for (int i = startBlock.getZ()+1; i <= playerPos.getZ()-1; i++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
            else if (sideHit == EnumFacing.NORTH) {
                for (int i = startBlock.getZ()-1; i >= playerPos.getZ()+1; i--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
            else if (sideHit == EnumFacing.EAST) {
                for (int i = startBlock.getX()+1; i <= playerPos.getX()-1; i++) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    //System.out.println("i: "+i+"....Pos: " + pos);
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
            else if (sideHit == EnumFacing.WEST) {
                for (int i = startBlock.getX()-1; i >= playerPos.getX()+1; i--) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
            else if (sideHit == EnumFacing.UP) {
                for (int i = startBlock.getY()+1; i <= playerPos.getY()-1; i++) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
            else if (sideHit == EnumFacing.DOWN) {
                for (int i = startBlock.getY()-1; i >= playerPos.getY()+1; i--) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world,pos)) {coordinates.add(pos);}
                }
            }
        }
        //***************************************************
        //PerpWall
        //***************************************************
        else if (mode == BuildingTool.toolModes.PerpWall) {
            if (sideHit == EnumFacing.UP) {
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
            else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
            else {
                for (int y = bound*-1; y <= bound; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
        }
        //***************************************************
        //VertCol
        //***************************************************
        else if (mode == BuildingTool.toolModes.VertCol) {
            if (sideHit == EnumFacing.UP) {
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() + y, startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else {
                for (int y = bound * -1; y <= bound; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorzCol
        //***************************************************
        else if (mode == BuildingTool.toolModes.HorzCol) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ()+z);
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ()-z);
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX()-x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX()+x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
            else {
                for (int y = bound * -1; y <= bound; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        return coordinates;
    }
}
