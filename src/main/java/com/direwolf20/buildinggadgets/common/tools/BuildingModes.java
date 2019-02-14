package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class BuildingModes {

//  TODO: Fix this
    private static boolean isReplaceable(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        if (!setBlock.isValidPosition(world, pos)) {
            return false;
        }
        if (pos.getY() < 0) {
            return false;
        }
        if (Config.GENERAL.allowOverwriteBlocks.get()) {
            if (!world.getBlockState(pos).isReplaceable(new BlockItemUseContext(
                    world, player, new ItemStack(setBlock.getBlock()), pos, EnumFacing.UP, 0.5F, 0.0F, 0.5F
            ))) {
                return false;
            }
        } else {
            if (world.getBlockState(pos).getMaterial() != Material.AIR) {
                return false;
            }
        }
        return false;
    }

    public static List<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, ItemStack tool) {
        //GadgetBuilding.ToolMode mode, IBlockState setBlock
        GadgetBuilding.ToolMode mode = GadgetBuilding.getToolMode(tool);
        IBlockState setBlock = GadgetUtils.getToolBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        List<BlockPos> coordinates = new ArrayList<BlockPos>();
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


        //***************************************************
        //Build to me
        //***************************************************
        if (mode == GadgetBuilding.ToolMode.BuildToMe) {
            if (sideHit == EnumFacing.SOUTH) {
                for (int i = startBlock.getZ() + 1; i <= playerPos.getZ() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.NORTH) {
                for (int i = startBlock.getZ() - 1; i >= playerPos.getZ() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int i = startBlock.getX() + 1; i <= playerPos.getX() - 1; i++) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int i = startBlock.getX() - 1; i >= playerPos.getX() + 1; i--) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.UP) {
                for (int i = startBlock.getY() + 1; i <= playerPos.getY() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int i = startBlock.getY() - 1; i >= playerPos.getY() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
                            if (isReplaceable(world, player, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, player, pos, setBlock)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                }
            } else {
                for (int y = bound; y >= bound * -1; y--) {
                    for (int x = boundX * -1; x <= boundX; x++) {
                        for (int z = boundZ * -1; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                            if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() + y, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = 1; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else {
                for (int y = bound * -1; y <= bound; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() - z);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int z = 1; z <= range; z++) {
                    for (int x = bound * -1; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() - z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    for (int x = bound * -1; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int x = bound * -1; x <= bound; x++) {
                    for (int z = bound * -1; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() - z);
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + 1);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - 1);
                    }
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = 1; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() + z);
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - 1);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + 1);
                    }
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - x, startBlock.getZ());
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX() + x - 1, startBlock.getY() + x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() - x + 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = 1; x <= range; x++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY() - x, startBlock.getZ());
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX() - x + 1, startBlock.getY() + x, startBlock.getZ());
                    } else {
                        pos = new BlockPos(startBlock.getX() + x - 1, startBlock.getY() + x, startBlock.getZ());
                    }
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //Checkerboard
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.Grid) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + 1, startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        //***************************************************
        //Surface
        //***************************************************
        else if (mode == GadgetBuilding.ToolMode.Surface) {
            IBlockState startState = world.getBlockState(startBlock);
            AxisAlignedBB area = new AxisAlignedBB(pos).grow(bound * (1 - Math.abs(sideHit.getXOffset())),
                    bound * (1 - Math.abs(sideHit.getYOffset())), bound * (1 - Math.abs(sideHit.getZOffset())));
            BlockPos locOffset;
            boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);
            if (GadgetGeneric.getConnectedArea(tool)) {
                addConnectedCoords(world, player, pos, startState, setBlock, sideHit, fuzzyMode, coordinates, new HashSet<>(),
                        (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
            } else {
                for (BlockPos loc : BlockPos.getAllInBox((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
                    locOffset = loc.offset(sideHit);
                    if ((fuzzyMode ? !world.isAirBlock(loc) : world.getBlockState(loc) == startState) && isReplaceable(world, player, locOffset, setBlock))
                        coordinates.add(locOffset);
                }
            }
        }
        return coordinates;
    }

    private static void addConnectedCoords(World world, EntityPlayer player, BlockPos loc, IBlockState state, IBlockState setBlock, EnumFacing sideHit,
            boolean fuzzyMode, List<BlockPos> coords, Set<BlockPos> coordsSearched, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coordsSearched.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        BlockPos locOffset = loc.offset(sideHit);
        if ((fuzzyMode ? world.isAirBlock(loc) : world.getBlockState(loc) != state) || !isReplaceable(world, player, locOffset, setBlock))
            return;

        coords.add(locOffset);
        coordsSearched.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, player, loc.add(x, y, z), state, setBlock, sideHit, fuzzyMode, coords, coordsSearched, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }

    public static List<BlockPos> sortByDistance(List<BlockPos> unSortedList, EntityPlayer player) {
        List<BlockPos> sortedList = new ArrayList<BlockPos>();
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
        //BoxedArray.reverse(distances);

        for (Double dist : distances) {
            //System.out.println(dist);
            sortedList.add(rangeMap.get(dist));
        }
        return sortedList;
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
        //BoxedArray.reverse(distances);

        for (Double dist : distances) {
            //System.out.println(dist);
            //sortedList.add(rangeMap.get(dist));
            BlockPos pos = new BlockPos(rangeMap.get(dist));
            sortedMap.add(new BlockMap(pos, PosToStateMap.get(pos), PosToX.get(pos), PosToY.get(pos), PosToZ.get(pos)));
        }
        //System.out.println(unSortedList);
        //System.out.println(sortedList);
        return sortedMap;
    }
}
