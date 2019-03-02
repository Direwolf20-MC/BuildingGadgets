package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingGridMode;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingSurfaceMode;
import com.direwolf20.buildinggadgets.common.building.modes.HorizontalColumnMode;
import com.direwolf20.buildinggadgets.common.building.modes.VerticalColumnMode;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
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

public enum ExchangingModes {

    Surface("surface.png", new ExchangingSurfaceMode(ExchangingModes::combineTester)),
    VerticalColumn("vertical_column.png", new VerticalColumnMode(ExchangingModes::combineTester)),
    HorizontalColumn("horizontal_column.png", new HorizontalColumnMode(ExchangingModes::combineTester)),
    Grid("grid.png", new ExchangingGridMode(ExchangingModes::combineTester));

    private final String displayName;
    private final ResourceLocation icon;
    private final IBuildingMode modeImpl;

    ExchangingModes(String iconFile, IBuildingMode modeImpl) {
        this.displayName = modeImpl.getLocalized();
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
        return modeImpl.getRegistryName().toString() + "/ExchangingGadget";
    }

    @Override
    public String toString() {
        return displayName;
    }

    public ExchangingModes next() {
        ExchangingModes[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static ExchangingModes byName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.getRegistryName().equals(name))
                .findFirst()
                .orElse(Surface);
    }

    private static final ImmutableList<ResourceLocation> ICONS = Arrays.stream(values())
            .map(ExchangingModes::getIcon)
            .collect(ImmutableList.toImmutableList());

    public static ImmutableList<ResourceLocation> getIcons() {
        return ICONS;
    }

    public static List<BlockPos> collectPlacementPos(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool, BlockPos initial) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return mode.createExecutionContext(player, hit, sideHit, tool).collectFilteredSequence(world, tool, player, initial);
    }

    public static BiPredicate<BlockPos, IBlockState> combineTester(World world, ItemStack tool, EntityPlayer player, BlockPos initial) {
        IBlockState initialBlockState = world.getBlockState(initial);
        IBlockState target = GadgetUtils.getToolBlock(tool);
        return (pos, state) -> {
            IBlockState worldBlockState = world.getBlockState(pos);

            //Don't try to replace for the same block
            if (worldBlockState == target) {
                return false;
            }

            //No need to replace if source and target are the same if fuzzy mode is off
            if (!GadgetGeneric.getFuzzy(tool) && worldBlockState != initialBlockState) {
                return false;
            }

            //If the target is already enqueued, don't replace it
            if (worldBlockState == ModBlocks.effectBlock.getDefaultState()) {
                return false;
            }
            //Only replace existing blocks, don't place more
            if (worldBlockState.getBlock().isAir(worldBlockState, world, pos)) {
                return false;
            }

            TileEntity tile = world.getTileEntity(pos);
            //Only replace construction block with same block state
            if (tile instanceof ConstructionBlockTileEntity) {
                if (((ConstructionBlockTileEntity) tile).getBlockState() == state) {
                    return false;
                }
            }
            //Otherwise if the block has a tile entity, ignore it
            if (tile != null) {
                return false;
            }

            //Bedrock, End Portal Frame, etc.
            if (worldBlockState.getBlockHardness(world, pos) < 0) {
                return false;
            }

            //Don't replace liquids
            return !worldBlockState.getMaterial().isLiquid();
        };
    }

}
