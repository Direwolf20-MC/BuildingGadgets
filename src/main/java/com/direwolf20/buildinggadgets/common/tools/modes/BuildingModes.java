package com.direwolf20.buildinggadgets.common.tools.modes;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
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
        return true;
    }

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
        //Build to me
        //***************************************************
        if (mode == GadgetBuilding.ToolMode.BuildToMe) {
            if (sideHit == EnumFacing.SOUTH) {
                for (int i = startBlock.getZ() + offset; i <= playerPos.getZ() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.NORTH) {
                for (int i = startBlock.getZ() - offset; i >= playerPos.getZ() + 1; i--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int i = startBlock.getX() + offset; i <= playerPos.getX() - 1; i++) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int i = startBlock.getX() - offset; i >= playerPos.getX() + 1; i--) {
                    pos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.UP) {
                for (int i = startBlock.getY() + offset; i <= playerPos.getY() - 1; i++) {
                    pos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int i = startBlock.getY() - offset; i >= playerPos.getY() + 1; i--) {
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
                for (int y = offset; y <= range; y++) {
                    for (int x = -boundX; x <= boundX; x++) {
                        for (int z = -boundZ; z <= boundZ; z++) {
                            pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + y, startBlock.getZ() + z);
                            if (isReplaceable(world, player, pos, setBlock)) {
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
                            if (isReplaceable(world, player, pos, setBlock)) {
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
                for (int y = offset; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() + y, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.DOWN) {
                for (int y = offset; y <= range; y++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else {
                for (int y = -bound; y <= bound; y++) {
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
                for (int z = offset; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = offset; z <= range; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() - z);
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = offset; x <= range; x++) {
                    pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, player, pos, setBlock)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = offset; x <= range; x++) {
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
                for (int z = offset; z <= range; z++) {
                    for (int x = -bound; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() - z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.SOUTH) {
                for (int z = offset; z <= range; z++) {
                    for (int x = -bound; x <= bound; x++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.EAST) {
                for (int x = offset; x <= range; x++) {
                    for (int z = -bound; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.WEST) {
                for (int x = offset; x <= range; x++) {
                    for (int z = -bound; z <= bound; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, player, pos, setBlock)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int x = -bound; x <= bound; x++) {
                    for (int z = -bound; z <= bound; z++) {
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
                for (int z = offset; z <= range; z++) {
                    if (startBlock.getY() > player.posY + 1) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() - z, startBlock.getZ() - z);
                    } else if (startBlock.getY() < player.posY - 2) {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() - z + offset);
                    } else {
                        pos = new BlockPos(startBlock.getX(), startBlock.getY() + z, startBlock.getZ() + z - offset);
                    }
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                    if (isReplaceable(world, player, pos, setBlock)) {
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
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY() + offset, startBlock.getZ() + z);
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
                addConnectedCoords(world, player, pos, onTop, startState, setBlock, sideHit, fuzzyMode, coordinates, new HashSet<>(),
                        (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
            } else {
                for (BlockPos loc : BlockPos.getAllInBox((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)) {
                    locOffset = onTop ? loc.offset(sideHit) : loc;
                    if ((fuzzyMode ? !world.isAirBlock(loc) : world.getBlockState(loc) == startState) && isReplaceable(world, player, locOffset, setBlock))
                        coordinates.add(locOffset);
                }
            }
        }
        return coordinates;
    }

    private static void addConnectedCoords(World world, EntityPlayer player, BlockPos loc, boolean onTop, IBlockState state, IBlockState setBlock, EnumFacing sideHit,
            boolean fuzzyMode, List<BlockPos> coords, Set<BlockPos> coordsSearched, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coordsSearched.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        BlockPos locOffset = onTop ? loc.offset(sideHit) : loc;
        if ((fuzzyMode ? world.isAirBlock(loc) : world.getBlockState(loc) != state) || !isReplaceable(world, player, locOffset, setBlock))
            return;

        coords.add(locOffset);
        coordsSearched.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, player, loc.add(x, y, z), onTop, state, setBlock, sideHit, fuzzyMode, coords, coordsSearched, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }
}
