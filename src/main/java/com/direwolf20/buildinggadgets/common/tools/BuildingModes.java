package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.building.implementation.IBuildingMode;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public enum BuildingModes implements IStringSerializable {

    BuildToMe((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        BlockPos playerPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
        EnumFacing.Axis axis = sideHit.getAxis();
        int limit = Math.abs(VectorTools.getAxisValue(playerPos, axis) - VectorTools.getAxisValue(hit, axis));

        // Don'modeImpl place block at where the player stands
        for (int i = 1; i < limit; i++) {
            BlockPos pos = hit.offset(sideHit, i);
            if (isReplaceable(world, pos, target)) {
                coordinates.add(pos);
            }
        }

        return coordinates;
    }),
    VerticalWall((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;
        EnumFacing facing = player.getHorizontalFacing();
        // NORTH/SOUTH -> EAST/WEST
        EnumFacing change = facing.rotateY();

        BlockPos center;
        if (sideHit.getAxis().isVertical()) {
            center = hit.offset(sideHit, radius + 1);
        } else {
            center = hit;
        }

        for (int y = -radius; y <= radius; y++) {
            BlockPos level = center.add(0, y, 0);
            // i could be either x or z
            for (int i = -radius; i <= radius; i++) {
                BlockPos pos = level.offset(change, i);
                if (isReplaceable(world, pos, target)) {
                    coordinates.add(pos);
                }
            }
        }

        return coordinates;
    }),
    VerticalColumn((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool);

        BlockPos bottom;
        switch (sideHit) {
            case DOWN:
                bottom = hit.down(range);
                break;
            case UP:
                bottom = hit.up(1);
                break;
            default:
                bottom = hit.down((range - 1) / 2);

                // This is used to simulate the feature where range=2 is equivalent to range=1 (etc.) when clicked on
                // side of a block
                if (range % 2 == 0) {
                    range--;
                }
                break;
        }

        for (int y = 0; y < range; y++) {
            BlockPos pos = bottom.up(y);
            if (isReplaceable(world, pos, target)) {
                coordinates.add(pos);
            }
        }

        return coordinates;
    }),
    HorizontalWall((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;

        EnumFacing change;
        BlockPos center;
        if (sideHit.getAxis().isVertical()) {
            center = hit;
            // Make the direction go horizontal
            sideHit = EnumFacing.NORTH;
            change = EnumFacing.EAST;
        } else {
            center = hit.offset(sideHit, radius + 1);
            // NORTH/SOUTH -> EAST/WEST
            change = sideHit.rotateY();
        }

        for (int i = -radius; i <= radius; i++) {
            BlockPos row = center.offset(sideHit, i);
            for (int j = -radius; j <= radius; j++) {
                BlockPos pos = row.offset(change, j);
                if (isReplaceable(world, pos, target)) {
                    coordinates.add(pos);
                }
            }
        }

        return coordinates;
    }),
    HorizontalColumn((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool);
        EnumFacing facing = player.getHorizontalFacing();

        for (int i = 1; i <= range; i++) {
            BlockPos pos = hit.offset(facing, i);
            if (isReplaceable(world, pos, target)) {
                coordinates.add(pos);
            }
        }

        return coordinates;
    }),
    Stairs((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool);

        if (sideHit.getAxis().isVertical()) {
            sideHit = player.getHorizontalFacing().getOpposite();
        }

        EnumFacing extension;
        EnumFacing verticalDirection;
        BlockPos base;
        if (hit.getY() > player.posY + 1) {
            extension = sideHit;
            verticalDirection = EnumFacing.DOWN;
            base = hit.down(1).offset(extension, 1);
        } else if (hit.getY() < player.posY - 2) {
            extension = sideHit;
            verticalDirection = EnumFacing.UP;
            base = hit.up(1);
        } else {
            extension = sideHit.getOpposite();
            verticalDirection = EnumFacing.UP;
            base = hit.up(1);
        }

        for (int i = 0; i < range; i++) {
            BlockPos pos = base.offset(extension, i).offset(verticalDirection, i);
            if (isReplaceable(world, pos, target)) {
                coordinates.add(pos);
            }
        }
        return coordinates;
    }),
    Grid((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        int range = GadgetUtils.getToolRange(tool) + 1;

        for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
            for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                if (x % (((range - 2) % 6) + 2) == 0 && z % (((range - 2) % 6) + 2) == 0) {
                    BlockPos pos = new BlockPos(hit.getX() + x, hit.getY() + 1, hit.getZ() + z);
                    if (isReplaceable(world, pos, target)) {
                        coordinates.add(pos);
                    }
                }
            }
        }

        return coordinates;
    }),
    Surface((world, player, hit, sideHit, tool) -> {
        List<BlockPos> coordinates = new ArrayList<>();
        IBlockState target = GadgetUtils.getToolActualBlock(tool);
        IBlockState filter = world.getBlockState(hit);
        int range = GadgetUtils.getToolRange(tool);
        int radius = (range - 1) / 2;

        Region area = new Region(hit).grow(
                radius * (1 - Math.abs(sideHit.getFrontOffsetX())),
                radius * (1 - Math.abs(sideHit.getFrontOffsetY())),
                radius * (1 - Math.abs(sideHit.getFrontOffsetZ())));
        BlockPos locOffset;
        boolean fuzzyMode = GadgetGeneric.getFuzzy(tool);

        if (GadgetGeneric.getConnectedArea(tool)) {
            addConnectedCoords(world, hit, filter, target, sideHit, fuzzyMode, coordinates, new HashSet<>(),
                    area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ);
        } else {
            for (BlockPos loc : BlockPos.getAllInBox(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
                locOffset = loc.offset(sideHit);
                if ((fuzzyMode ? !world.isAirBlock(loc) : world.getBlockState(loc) == filter) && isReplaceable(world, locOffset, target)) {
                    coordinates.add(locOffset);
                }
            }
        }

        return coordinates;
    });

    private final String displayName;
    private final IBuildingMode modeImpl;

    BuildingModes(IBuildingMode modeImpl) {
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

    public BuildingModes next() {
        BuildingModes[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static List<BlockPos> getAffectiveBuildingPositions(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode"));
        return mode.computeCoordinates(player, hit, sideHit, tool).collect();
    }

    public static IBuildingMode byName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find building mode with name " + name))
                .modeImpl;
    }

    public static boolean isReplaceable(World world, BlockPos pos, IBlockState target) {
        if (!target.getBlock().canPlaceBlockAt(world, pos) || world.isOutsideBuildHeight(pos)) {
            return false;
        }
        if (SyncedConfig.canOverwriteBlocks) {
            return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
        } else {
            return world.isAirBlock(pos);
        }
    }

    private static final Function<World, BiPredicate<BlockPos, IBlockState>> VALIDATOR_FACTORY = world -> (pos, state) -> isReplaceable(world, pos, state);

    private static void addConnectedCoords(World world, BlockPos loc, IBlockState state, IBlockState setBlock, EnumFacing sideHit,
                                           boolean fuzzyMode, List<BlockPos> coords, Set<BlockPos> coordsSearched, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coordsSearched.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        BlockPos locOffset = loc.offset(sideHit);
        if ((fuzzyMode ? world.isAirBlock(loc) : world.getBlockState(loc) != state) || !isReplaceable(world, locOffset, setBlock))
            return;

        coords.add(locOffset);
        coordsSearched.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, loc.add(x, y, z), state, setBlock, sideHit, fuzzyMode, coords, coordsSearched, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
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
