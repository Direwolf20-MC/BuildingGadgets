package com.direwolf20.buildinggadgets.common.util.tools.modes;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.api.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    public static List<BlockPos> collectPlacementPos(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, BlockPos initial) {
        IBuildingMode mode = byName(NBTHelper.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return mode.createExecutionContext(player, hit, sideHit, tool).collectFilteredSequence(world, tool, player, initial);
    }

    public static BiPredicate<BlockPos, IBlockState> combineTester(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        IBlockState initialBlockState = world.getBlockState(initial);
        IBlockState target = GadgetUtils.getToolBlock(tool);
        return (pos, state) -> {
            IBlockState worldBlockState = world.getBlockState(pos);

            // Don't try to replace for the same block
            if (worldBlockState == target)
                return false;

            // No need to replace if source and target are the same if fuzzy mode is off
            if (!GadgetGeneric.getFuzzy(tool) && worldBlockState != initialBlockState)
                return false;

            // If the target is already enqueued, don't replace it
            if (worldBlockState == BGBlocks.effectBlock.getDefaultState())
                return false;
            // Only replace existing blocks, don't place more
            if (worldBlockState.getBlock().isAir(worldBlockState, world, pos))
                return false;

            TileEntity tile = world.getTileEntity(pos);
            // Only replace construction block with same block state
            if (tile instanceof ConstructionBlockTileEntity && ((ConstructionBlockTileEntity) tile).getBlockState() == state)
                return false;
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
