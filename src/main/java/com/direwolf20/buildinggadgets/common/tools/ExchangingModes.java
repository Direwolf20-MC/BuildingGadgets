package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.building.IBuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.GridMode;
import com.direwolf20.buildinggadgets.common.building.modes.HorizontalColumnMode;
import com.direwolf20.buildinggadgets.common.building.modes.SurfaceMode;
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

    Surface("surface.png", new SurfaceMode(ExchangingModes::combineTester)),
    VerticalColumn("vertical_column.png", new VerticalColumnMode(ExchangingModes::combineTester)),
    HorizontalColumn("horizontal_column.png", new HorizontalColumnMode(ExchangingModes::combineTester)),
    Grid("grid.png", new GridMode(ExchangingModes::combineTester));

    private final String displayName;
    private final ResourceLocation icon;
    private final IBuildingMode modeImpl;

    ExchangingModes(String iconFile, IBuildingMode modeImpl) {
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

    public static List<BlockPos> collectPlacementPos(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        IBuildingMode mode = byName(NBTTool.getOrNewTag(tool).getString("mode")).getModeImplementation();
        return mode.createExecutionContext(player, hit, sideHit, tool).collectFilteredSequence(world);
    }

    public static BiPredicate<BlockPos, IBlockState> combineTester(World world) {
        //TODO product filter with fuzzy mode
        return (pos, state) -> true;
    }

    //TODO use predicate
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
        if (te instanceof ConstructionBlockTileEntity) {
            if (((ConstructionBlockTileEntity) te).getBlockState() == target) {
                return false;
            }
        } else {
            return false;
        }
        if (worldBlockState.getBlockHardness(world, pos) < 0) {
            return false;
        }
        if (worldBlockState.getMaterial() == Material.AIR) {
            return false;
        }
        return !worldBlockState.getMaterial().isLiquid();
    }
}
