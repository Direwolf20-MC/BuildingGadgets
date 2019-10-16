package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

public enum ExchangingMode {
    SURFACE("surface.png", new ExchangingSurfaceMode(ExchangingMode::combineTester)),
    VERTICAL_COLUMN("vertical_column.png", new ExchangingVerticalColumnMode(ExchangingMode::combineTester)),
    HORIZONTAL_COLUMN("horizontal_column.png", new ExchangingHorizontalColumnMode(ExchangingMode::combineTester)),
    GRID("grid.png", new ExchangingGridMode(ExchangingMode::combineTester));
    private static final ExchangingMode[] VALUES = values();
    private final ResourceLocation icon;
    private final IBuildingMode modeImpl;

    ExchangingMode(String iconFile, IBuildingMode modeImpl) {
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
        return getModeImplementation().getRegistryName().toString() + "/ExchangingGadget";
    }

    @Override
    public String toString() {
        return getModeImplementation().getLocalizedName();
    }

    public ExchangingMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public static ExchangingMode byName(String name) {
        return Arrays.stream(VALUES)
                .filter(mode -> mode.getRegistryName().equals(name))
                .findFirst()
                .orElse(SURFACE);
    }

    private static final ImmutableList<ResourceLocation> ICONS = Arrays.stream(VALUES)
            .map(ExchangingMode::getIcon)
            .collect(ImmutableList.toImmutableList());

    public static ImmutableList<ResourceLocation> getIcons() {
        return ICONS;
    }

    public static List<BlockPos> collectPlacementPos(IWorld world, PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool, BlockPos initial) {
        IBuildingMode mode = byName(NBTHelper.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return mode.createExecutionContext(player, hit, sideHit, tool, initial).collectFilteredSequence();
    }

    public static BiPredicate<BlockPos, BlockData> combineTester(IWorld world, ItemStack tool, PlayerEntity player, BlockPos initial) {
        BlockState initialBlockState = world.getBlockState(initial);
        BlockData target = GadgetUtils.getToolBlock(tool);
        return (pos, data) -> {
            BlockState worldBlockState = world.getBlockState(pos);

            // Don't try to replace for the same block
            if (worldBlockState == target.getState())
                return false;

            // No need to replace if source and target are the same if fuzzy mode is off
            if (!AbstractGadget.getFuzzy(tool) && worldBlockState != initialBlockState)
                return false;

            // If the target is already enqueued, don't replace it
            if (worldBlockState == OurBlocks.effectBlock.getDefaultState())
                return false;
            // Only replace existing blocks, don't place more
            if (worldBlockState.isAir(world, pos))
                return false;

            TileEntity tile = world.getTileEntity(pos);
            // Only replace construction block with same block state
            if (tile instanceof ConstructionBlockTileEntity) {
                if (((ConstructionBlockTileEntity) tile).getConstructionBlockData().equals(data))
                    return false;
            }
            else if (tile != null) // Otherwise if the block has a tile entity, ignore it
                return false;

            // Bedrock, End Portal Frame, etc.
            if (worldBlockState.getBlockHardness(world, pos) < 0)
                return false;

            // Don't replace liquids
            return !worldBlockState.getMaterial().isLiquid();
        };
    }

}
