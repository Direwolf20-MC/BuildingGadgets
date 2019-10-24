package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper.Blocks;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum BuildingMode {
    TARGETED_AXIS_CHASING("build_to_me.png", new BuildToMeMode(BuildingMode::combineTester)),
    VERTICAL_COLUMN("vertical_column.png", new BuildingVerticalColumnMode(BuildingMode::combineTester)),
    HORIZONTAL_COLUMN("horizontal_column.png", new BuildingHorizontalColumnMode(BuildingMode::combineTester)),
    VERTICAL_WALL("vertical_wall.png", new VerticalWallMode(BuildingMode::combineTester)),
    HORIZONTAL_WALL("horizontal_wall.png", new HorizontalWallMode(BuildingMode::combineTester)),
    STAIR("stairs.png", new StairMode(BuildingMode::combineTester)),
    GRID("grid.png", new GridMode(BuildingMode::combineTester)),
    SURFACE("surface.png", new BuildingSurfaceMode(BuildingMode::combineTester));
    private static final BuildingMode[] VALUES = values();
    private final ResourceLocation icon;
    private final IBuildingMode modeImpl;

    BuildingMode(String iconFile, IBuildingMode modeImpl) {
        this.icon = new ResourceLocation(Reference.MODID, "textures/gui/mode/" + iconFile);
        this.modeImpl = modeImpl;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public IBuildingMode getModeImplementation() {
        return modeImpl;
    }

    public String getRegistryName() {
        return getModeImplementation().getRegistryName().toString() + "/BuildingGadget";
    }

    @Override
    public String toString() {
        return getModeImplementation().getLocalizedName();
    }

    public BuildingMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public static List<BlockPos> collectPlacementPos(IWorld world, PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool, BlockPos initial) {
        IBuildingMode mode = byName(NBTHelper.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return Blocks.byDistance(mode.createExecutionContext(player, hit, sideHit, tool, initial).getFilteredSequence().stream(), Function.identity(), player)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static BuildingMode byName(String name) {
        return Arrays.stream(VALUES)
                .filter(mode -> mode.getRegistryName().equals(name))
                .findFirst()
                .orElse(TARGETED_AXIS_CHASING);
    }

    private static final ImmutableList<ResourceLocation> ICONS = Arrays.stream(VALUES)
            .map(BuildingMode::getIcon)
            .collect(ImmutableList.toImmutableList());

    public static ImmutableList<ResourceLocation> getIcons() {
        return ICONS;
    }

    public static BiPredicate<BlockPos, BlockData> combineTester(IWorld world, ItemStack tool, PlayerEntity player, BlockPos original) {
        BlockData target = GadgetUtils.getToolBlock(tool);
        return (pos, data) -> {
            BlockState current = world.getBlockState(pos);
            if (current.getBlock() == OurBlocks.effectBlock) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof EffectBlockTileEntity && ((EffectBlockTileEntity) te).getRenderedBlock() != null)
                    current = ((EffectBlockTileEntity) te).getRenderedBlock().getState();
            }

            // Filter out situations where people try to create floating grass (etc.)
            if (! target.getState().isValidPosition(world, pos))
                return false;


            // World boundary check
            if (pos.getY() < 0)
                return false;

            // If we allow overrides, replaceable blocks (e.g. grass, water) will return true
            if (Config.GENERAL.allowOverwriteBlocks.get())
                // Is the current block replaceable by the target block in the given context?
                return current.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, VectorHelper.getLookingAt(player, tool))));
            // If we don't allow overrides, replacement only happens when the current position is air
            return current.isAir(world, pos);
        };
    }

}
