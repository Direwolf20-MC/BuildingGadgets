package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ExchangingModes {
    private static boolean isReplaceable(World world, BlockPos pos, IBlockState currentBlock, IBlockState setBlock, boolean fuzzyMode) {
        IBlockState worldBlockState = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (worldBlockState != currentBlock && !fuzzyMode) {
            return false;
        }
        if (worldBlockState == ModBlocks.effectBlock.getDefaultState()) {
            return false;
        }
        if (worldBlockState == setBlock) {
            return false;
        }
        if (te != null && !(te instanceof ConstructionBlockTileEntity)) {
            return false;
        }
        if (te instanceof ConstructionBlockTileEntity) {
            if (((ConstructionBlockTileEntity) te).getBlockState() == setBlock) {
                return false;
            }
        }
        if (worldBlockState.getBlockHardness(world, pos) < 0) {
            return false;
        }
        if (worldBlockState.getMaterial() == Material.AIR) {
            return false;
        }
        if (worldBlockState.getMaterial().isLiquid()) {
            return false;
        }
        /*if (world.getBlockState(pos) != currentBlock || world.getBlockState(pos) == ModBlocks.effectBlock.getDefaultState() || world.getBlockState(pos) == setBlock || world.getTileEntity(pos) != null || currentBlock.getBlock().getBlockHardness(currentBlock, world, pos) < 0) {
            return false;
        }*/
        return true;
    }

    public static void addConnectedCoords(World world, BlockPos loc, IBlockState state, IBlockState setBlock,
                                          boolean fuzzyMode, List<BlockPos> coords, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coords.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ) {
            return;
        }

        if (!isReplaceable(world, loc, state, setBlock, fuzzyMode)) {
            return;
        }

        coords.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, loc.add(x, y, z), state, setBlock, fuzzyMode, coords, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }

    public static List<BlockPos> getBuildOrders(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, ItemStack tool) {

        GadgetExchanger.ToolMode mode = GadgetExchanger.getToolMode(tool);
        IBlockState setBlock = GadgetUtils.getToolBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        List<BlockPos> coordinates = new ArrayList<BlockPos>();
//        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        BlockPos pos = startBlock;
        int bound = (range - 1) / 2;
        EnumFacing playerFacing = player.getHorizontalFacing();
        int boundX, boundZ, boundXS, boundZS;
        if (playerFacing == EnumFacing.SOUTH || playerFacing == EnumFacing.NORTH) {
            boundX = bound;
            boundZ = 0;
        } else {
            boundX = 0;
            boundZ = bound;
        }
        if (sideHit == EnumFacing.SOUTH || sideHit == EnumFacing.NORTH) {
            boundXS = bound;
            boundZS = 0;
        } else {
            boundXS = 0;
            boundZS = bound;
        }

        IBlockState currentBlock = world.getBlockState(startBlock);

        //***************************************************
        //Surface
        //***************************************************
        if (mode == GadgetExchanger.ToolMode.Surface) {
            if (GadgetGeneric.getConnectedArea(tool)) {
                AxisAlignedBB area = new AxisAlignedBB(pos).grow(bound * (1 - Math.abs(sideHit.getFrontOffsetX())),
                        bound * (1 - Math.abs(sideHit.getFrontOffsetY())), bound * (1 - Math.abs(sideHit.getFrontOffsetZ())));
                addConnectedCoords(world, pos, currentBlock, setBlock, fuzzyMode, coordinates,
                        (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
            } else {
                if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                    for (int x = bound * -1; x <= bound; x++) {
                        for (int z = bound * -1; z <= bound; z++) {
                            pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                            if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                                coordinates.add(pos);
                            }
                        }
                    }
                } else {
                    for (int y = bound; y >= bound * -1; y--) {
                        for (int x = boundXS * -1; x <= boundXS; x++) {
                            for (int z = boundZS * -1; z <= boundZS; z++) {
                                pos = new BlockPos(startBlock.getX() + x, startBlock.getY() - y, startBlock.getZ() + z);
                                //System.out.println(pos);
                                if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                                    coordinates.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        }
        //***************************************************
        //VerticalColumn
        //***************************************************
        if (mode == GadgetExchanger.ToolMode.VerticalColumn) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                for (int x = boundZ * -1; x <= boundZ; x++) {
                    for (int z = boundX * -1; z <= boundX; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else {
                for (int y = bound; y >= bound * -1; y--) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
                    if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //HorizontalColumn
        //***************************************************
        if (mode == GadgetExchanger.ToolMode.HorizontalColumn) {
            if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
                for (int x = boundX * -1; x <= boundX; x++) {
                    for (int z = boundZ * -1; z <= boundZ; z++) {
                        pos = new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            } else if (sideHit == EnumFacing.NORTH || sideHit == EnumFacing.SOUTH) {
                for (int x = bound * -1; x <= bound; x++) {
                    pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ());
                    if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            } else if (sideHit == EnumFacing.EAST || sideHit == EnumFacing.WEST) {
                for (int z = bound * -1; z <= bound; z++) {
                    pos = new BlockPos(startBlock.getX(), startBlock.getY(), startBlock.getZ() + z);
                    if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            }
        }
        //***************************************************
        //TorchPlacer
        //***************************************************
        else if (mode == GadgetExchanger.ToolMode.Grid) {
            range++;
            for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
                for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                    if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                        pos = new BlockPos(startBlock.getX() + x, startBlock.getY(), startBlock.getZ() + z);
                        if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
                            coordinates.add(pos);
                        }
                    }
                }
            }
        }
        return coordinates;
    }
}
