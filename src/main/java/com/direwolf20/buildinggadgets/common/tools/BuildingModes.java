package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.gadgets.GadgetGeneric;
import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class BuildingModes {
    public static List<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, ItemStack tool) {
        //GadgetBuilding.ToolMode mode, IBlockState setBlock
        GadgetBuilding.ToolMode mode = GadgetBuilding.getToolMode(tool);
        IBlockState setBlock = GadgetUtils.getToolBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        BlockPos pos = startBlock;
        boolean onTop = GadgetBuilding.shouldPlaceAtop(tool);
        int offset = onTop ? 1 : 0;
        if (mode != GadgetBuilding.ToolMode.BuildToMe && mode != GadgetBuilding.ToolMode.Grid && mode != GadgetBuilding.ToolMode.Surface)
            range -= 1 - offset;

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
        //***************************************************
        //Build to me - done
        //***************************************************
        if (mode == GadgetBuilding.ToolMode.BuildToMe) {
            if (sideHit == EnumFacing.SOUTH) {
                for (int i = startBlock.getZ() + offset; i <= playerPos.getZ() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.NORTH) {
                for (int i = startBlock.getZ() - offset; i >= playerPos.getZ() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int i = startBlock.getX() + offset; i <= playerPos.getX() - 1; i++) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int i = startBlock.getX() - offset; i >= playerPos.getX() + 1; i--) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.UP) {
                for (int i = startBlock.getY() + offset; i <= playerPos.getY() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int i = startBlock.getY() - offset; i >= playerPos.getY() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //VerticalWall
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.VerticalWall) {
            if (sideHit == EnumFacing.UP) {
                for (int y = offset; y <= range; y++) {
                    for (int x = -boundX; x <= boundX; x++) {
                        for (int z = -boundZ; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = offset; y <= range; y++) {
                    for (int x = -boundX; x <= boundX; x++) {
                        for (int z = -boundZ; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else {
                for (int y = bound; y >= -bound; y--) {
                    for (int x = -boundX; x <= boundX; x++) {
                        for (int z = -boundZ; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            }
        }
        //***************************************************
        //VerticalColumn
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.VerticalColumn) {
            if (sideHit == EnumFacing.UP) {
                for (int y = offset; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() + y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = offset; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else {
                for (int y = -bound; y <= bound; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalColumn
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.HorizontalColumn) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = offset; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = offset; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() - z);
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = offset; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = offset; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalWall
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.HorizontalWall) {
            /*if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }*/
            if (sideHit == EnumFacing.NORTH) {
                for (int z = offset; z <= range; z++) {
                    for (int x = -bound; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() - z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = offset; z <= range; z++) {
                    for (int x = -bound; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = offset; x <= range; x++) {
                    for (int z = -bound; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = offset; x <= range; x++) {
                    for (int z = -bound; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int x = -bound; x <= bound; x++) {
                    for (int z = -bound; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        //***************************************************
        //Stairs
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.Stairs) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                sideHit = playerFacing.getOpposite();
            }
            if (sideHit == EnumFacing.NORTH) {
                for (int z = offset; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() - z);
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + offset);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - offset);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = offset; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() + z);
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - offset);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + offset);
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = offset; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - x, startBlock.getZ());
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX() + x - offset, startBlock.getY() + x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() - x + offset, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = offset; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY() - x, startBlock.getZ());
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX() - x + offset, startBlock.getY() + x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() + x - offset, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //Checkerboard - done
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.Grid) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + offset, startBlock.getZ() + z);
                        if (isReplaceable(world, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        //***************************************************
        //Surface - done
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.Surface) {
            IBlockState startState = world.getBlockState(startBlock);
            AxisAlignedBB area = new AxisAlignedBB(pos).grow(bound * (1 - Math.abs(sideHit.getFrontOffsetX())),
                    bound * (1 - Math.abs(sideHit.getFrontOffsetY())), bound * (1 - Math.abs(sideHit.getFrontOffsetZ())));
            BlockPos locOffset;
            boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);
            if (GadgetGeneric.getConnectedArea(tool)) {
                addConnectedCoords(world, pos, onTop, startState, setBlock, sideHit, fuzzyMode, coordinates, new HashSet<>(),
                        (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
            } else {
                for (BlockPos loc : BlockPos.getAllInBox((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
                    locOffset = onTop ? loc.offset(sideHit) : loc;
                    if ((fuzzyMode ? !world.isAirBlock(loc) : world.getBlockState(loc) == startState) && isReplaceable(world, locOffset, setBlock))
                        coordinates.add(locOffset);
                }
            }
        }
        return coordinates;
    }

    private static void addConnectedCoords(World world, BlockPos loc, boolean onTop, IBlockState state, IBlockState setBlock, EnumFacing sideHit,
            boolean fuzzyMode, List<BlockPos> coords, Set<BlockPos> coordsSearched, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coordsSearched.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        BlockPos locOffset = onTop ? loc.offset(sideHit) : loc;
        if ((fuzzyMode ? world.isAirBlock(loc) : world.getBlockState(loc) != state) || !isReplaceable(world, locOffset, setBlock))
            return;

        coords.add(locOffset);
        coordsSearched.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, loc.add(x, y, z), onTop, state, setBlock, sideHit, fuzzyMode, coords, coordsSearched, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }

    private static boolean isReplaceable(World world, BlockPos pos, IBlockState setBlock) {
        if (!setBlock.getBlock().canPlaceBlockAt(world, pos)) {
            return false;
        }
        if (pos.getY() < 0) {
            return false;
        }
        if (SyncedConfig.canOverwriteBlocks) {
            if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                return false;
            }
        } else {
            if (world.getBlockState(pos).getMaterial() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    public static List<BlockMap> sortMapByDistance(List<BlockMap> unSortedMap, EntityPlayer player) {//TODO unused
        List<BlockPos> unSortedList = new ArrayList<BlockPos>();
//        List<BlockPos> sortedList = new ArrayList<BlockPos>();
        Map<BlockPos, IBlockState> PosToStateMap = new HashMap<BlockPos, IBlockState>();
        Map<BlockPos, Integer> PosToX = new HashMap<BlockPos, Integer>();
        Map<BlockPos, Integer> PosToY = new HashMap<BlockPos, Integer>();
        Map<BlockPos, Integer> PosToZ = new HashMap<BlockPos, Integer>();
        for (BlockMap blockMap : unSortedMap) {
            PosToStateMap.put(blockMap.pos, blockMap.state);
            PosToX.put(blockMap.pos, blockMap.xOffset);
            PosToY.put(blockMap.pos, blockMap.yOffset);
            PosToZ.put(blockMap.pos, blockMap.zOffset);
            unSortedList.add(blockMap.pos);
        }
        List<BlockMap> sortedMap = new ArrayList<BlockMap>();
        Double2ObjectMap<BlockPos> rangeMap = new Double2ObjectArrayMap<>(unSortedList.size());
        DoubleSortedSet distances = new DoubleRBTreeSet();
        double x = player.posX;
        double y = player.posY + player.getEyeHeight();
        double z = player.posZ;
        for (BlockPos pos : unSortedList) {
            double distance = pos.distanceSqToCenter(x, y, z);
            rangeMap.put(distance, pos);
            distances.add(distance);
        }
        for (double dist : distances) {
            //System.out.println(dist);
            BlockPos pos = new BlockPos(rangeMap.get(dist));
            sortedMap.add(new BlockMap(pos, PosToStateMap.get(pos), PosToX.get(pos), PosToY.get(pos), PosToZ.get(pos)));
        }
        //System.out.println(unSortedList);
        //System.out.println(sortedList);
        return sortedMap;
    }
}
