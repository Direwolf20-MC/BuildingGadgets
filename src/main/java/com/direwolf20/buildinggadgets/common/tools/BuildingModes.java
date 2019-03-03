package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.*;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.google.common.collect.ImmutableList;
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

    BuildToMe("build_to_me.png", new TargetedAxisChasingMode(BuildingModes::combineTester)),
    VerticalColumn("vertical_column.png", new VerticalColumnMode(BuildingModes::combineTester)),
    HorizontalColumn("horizontal_column.png", new BuildingHorizontalColumnMode(BuildingModes::combineTester)),
    VerticalWall("vertical_wall.png", new VerticalWallMode(BuildingModes::combineTester)),
    HorizontalWall("horizontal_wall.png", new HorizontalWallMode(BuildingModes::combineTester)),
    Stair("stairs.png", new StairMode(BuildingModes::combineTester)),
    Grid("grid.png", new GridMode(BuildingModes::combineTester)),
    Surface("surface.png", new BuildingSurfaceMode(BuildingModes::combineTester));

    private final String displayName;
    private final ResourceLocation icon;
    private final BuildingModeWrapper modeImpl;

    BuildingModes(String iconFile, IBuildingMode modeImpl) {
        this.displayName = modeImpl.getLocalized();
        this.icon = new ResourceLocation(BuildingGadgets.MODID, "textures/ui/" + iconFile);
        boolean supportAtop = modeImpl instanceof IAtopSupport;
        this.modeImpl = new BuildingModeWrapper(modeImpl, supportAtop ? (IAtopSupport) modeImpl : BuildingModeWrapper.NONE);
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public IBuildingMode getModeImplementation() {
        return modeImpl;
    }

    public boolean doesSupportAtop() {
        return modeImpl.doesSupportAtop();
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

    public static List<BlockPos> collectPlacementPos(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, BlockPos initial) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return mode.createExecutionContext(player, hit, sideHit, tool).collectFilteredSequence(world, tool, player, initial);
    }

    public static BuildingModes byName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.getRegistryName().equals(name))
                .findFirst()
                .orElse(BuildToMe);
    }

    private static final ImmutableList<ResourceLocation> ICONS = Arrays.stream(values())
            .map(BuildingModes::getIcon)
            .collect(ImmutableList.toImmutableList());

    public static ImmutableList<ResourceLocation> getIcons() {
        return ICONS;
    }

    public static BiPredicate<BlockPos, IBlockState> combineTester(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        IBlockState target = GadgetUtils.getToolBlock(tool);
        return (pos, state) -> {
            IBlockState current = world.getBlockState(pos);
            if (!target.getBlock().canPlaceBlockAt(world, pos))
                return false;
            if (pos.getY() < 0)
                return false;
            if (SyncedConfig.canOverwriteBlocks)
                return current.getBlock().isReplaceable(world, pos);
            return current.getBlock().isAir(current, world, pos);
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
