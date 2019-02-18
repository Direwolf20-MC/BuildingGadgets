package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.building.implementation.IBuildingMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ExchangingModes implements IStringSerializable {

    Surface((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        IBlockState current = world.getBlockState(hit);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        Region area = new Region(hit).grow(
                radius * (1 - Math.abs(sideHit.getFrontOffsetX())),
                radius * (1 - Math.abs(sideHit.getFrontOffsetY())),
                radius * (1 - Math.abs(sideHit.getFrontOffsetZ())));
        if (GadgetGeneric.getConnectedArea(tool)) {
            addConnectedCoords(world, hit, current, target, fuzzyMode, coordinates,
                    area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ);
        } else {
            for (BlockPos pos : BlockPos.getAllInBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
                if (isReplaceable(world, pos, current, target, fuzzyMode)) {
                    coordinates.add(pos);
                }
            }
        }

        return coordinates;
    }),
    VerticalColumn((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        IBlockState current = world.getBlockState(hit);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        BlockPos base;
        EnumFacing extension;
        if (sideHit.getAxis().isVertical()) {
            EnumFacing playerFacing = player.getHorizontalFacing();
            base = hit.offset(playerFacing.getOpposite(), radius);
            extension = playerFacing;
        } else {
            base = hit.down(radius);
            extension = EnumFacing.UP;
        }

        for (int i = 0; i < range; i++) {
            BlockPos pos = base.offset(extension, i);
            if (isReplaceable(world, pos, current, target, fuzzyMode)) {
                coordinates.add(pos);
            }
        }

        return coordinates;
    }),
    HorizontalColumn((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        IBlockState current = world.getBlockState(hit);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        EnumFacing playerFacing = player.getHorizontalFacing();
        EnumFacing extension = playerFacing.rotateY();
        BlockPos base = hit.offset(extension.getOpposite(), radius);

        for (int i = 0; i < range; i++) {
            BlockPos pos = base.offset(extension, i);
            if (isReplaceable(world, pos, current, target, fuzzyMode)) {
                coordinates.add(pos);
            }
        }

        return coordinates;
    }),
    Grid((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        IBlockState current = world.getBlockState(hit);
        int range = GadgetUtils.getToolRange(tool) + 1;
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
            for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                    BlockPos pos = new BlockPos(hit.getX() + x, hit.getY(), hit.getZ() + z);
                    if (isReplaceable(world, pos, current, target, fuzzyMode)) {
                        coordinates.add(pos);
                    }
                }
            }
        }

        return coordinates;
    });

    private final String displayName;
    private final IBuildingMode modeImpl;

    ExchangingModes(IBuildingMode modeImpl) {
        this.displayName = GadgetGeneric.formatName(name());
        this.modeImpl = modeImpl;
    }

    /**
     * Human-readable display display name.
     */
    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public ExchangingModes next() {//TODO unused
        ExchangingModes[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static IBuildingMode byName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find exchanging mode with name " + name))
                .modeImpl;
    }

    public static List<BlockPos> getBuildCoords(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode"));
        return mode.computeCoordinates(world, player, hit, sideHit, tool);
    }

    private static boolean isReplaceable(World world, BlockPos pos, IBlockState current, IBlockState target, boolean fuzzyMode) {
        IBlockState worldBlockState = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        if (worldBlockState != current && !fuzzyMode) {
            return false;
        }
        if (worldBlockState == ModBlocks.effectBlock.getDefaultState()) {
            return false;
        }
        if (worldBlockState == target) {
            return false;
        }
        if (te != null && !(te instanceof ConstructionBlockTileEntity)) {
            return false;
        }
        if (te != null) {
            if (((ConstructionBlockTileEntity) te).getBlockState() == target) {
                return false;
            }
        }
        if (worldBlockState.getBlockHardness(world, pos) < 0) {
            return false;
        }
        if (worldBlockState.getMaterial() == Material.AIR) {
            return false;
        }
        return !worldBlockState.getMaterial().isLiquid();
    }

    public static void addConnectedCoords(World world, BlockPos loc, IBlockState state, IBlockState setBlock,
            boolean fuzzyMode, List<BlockPos> coords, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coords.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        if (!isReplaceable(world, loc, state, setBlock, fuzzyMode))
            return;

        coords.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, loc.add(x, y, z), state, setBlock, fuzzyMode, coords, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }
}
