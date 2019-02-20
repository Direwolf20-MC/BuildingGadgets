package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.modes.GridMode;
import com.direwolf20.buildinggadgets.common.building.modes.HorizontalColumnMode;
import com.direwolf20.buildinggadgets.common.building.modes.StairMode;
import com.direwolf20.buildinggadgets.common.building.modes.SurfaceMode;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiPredicate;

public enum BuildingModes {

    BuildToMe("build_to_me.png", new HorizontalColumnMode(BuildingModes::combineTester)),
    VerticalWall("vertical_wall.png", new HorizontalColumnMode(BuildingModes::combineTester)),
    VerticalColumn("vertical_column.png", new HorizontalColumnMode(BuildingModes::combineTester)),
    HorizontalWall("horizontal_wall.png", new HorizontalColumnMode(BuildingModes::combineTester)),
    HorizontalColumn("horizontal_column.png", new HorizontalColumnMode(BuildingModes::combineTester)),
    Stair("stairs.png", new StairMode(BuildingModes::combineTester)),
    Grid("grid.png", new GridMode(BuildingModes::combineTester)),
    Surface("surface.png", new SurfaceMode(BuildingModes::combineTester));

    private final String displayName;
    private final ResourceLocation icon;
    private final IBuildingMode modeImpl;

    BuildingModes(String iconFile, IBuildingMode modeImpl) {
        this.displayName = GadgetGeneric.formatName(name());
        this.icon = new ResourceLocation(BuildingGadgets.MODID, "textures/ui/" + iconFile);
        this.modeImpl = modeImpl;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public IBuildingMode getModeImplementation() {
        return modeImpl;
    }

    public String getRegistryName() {
        return modeImpl.getRegistryName().toString() + "/BuildingGadget";
    }

    @Override
    public String toString() {
        return displayName;
    }

    public BuildingModes next() {
        BuildingModes[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static List<BlockPos> collectPlacementPos(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode"));
        return mode.computeCoordinates(player, hit, sideHit, tool).collect();
    }

    public static IBuildingMode byName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.getRegistryName().equals(name))
                .findFirst()
                .orElse(BuildToMe)
                .getModeImplementation();
    }

    public static BiPredicate<BlockPos, IBlockState> combineTester(World world) {
        return (pos, state) -> {
            if (!state.getBlock().canPlaceBlockAt(world, pos) || world.isOutsideBuildHeight(pos)) {
                return false;
            }
            if (SyncedConfig.canOverwriteBlocks) {
                return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
            } else {
                return world.isAirBlock(pos);
            }
        };
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
