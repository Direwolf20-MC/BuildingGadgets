package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class BuildingValidators {
    /**
     * Validates for the building gadget over the collected block pos set
     */
    public static final Predicate<BuildingActionContext> BUILDING_VALIDATOR = (actionContext) -> {
        final BuildingContext context = actionContext.getContext();
        final BlockPos pos = actionContext.getCurrentPos();

        if (!context.getWorldState(pos).isReplaceable(context.createBlockUseContext(actionContext.getPlayer())))
            return false;

        if (World.isOutsideBuildHeight(pos))
            return false;

        return Config.GENERAL.allowOverwriteBlocks.get()
            ? context.getWorldState(pos).getMaterial().isReplaceable()
            : context.getWorldState(pos).getMaterial() != Material.AIR;
    };

    /**
     * Validates for the exchanger over the collected block pos set.
     * Much more complex flow of validation rules as we attempt to fix a lot of
     * common issues that show up when you exchange blocks in mc.
     */
    public static final Predicate<BuildingActionContext> EXCHANGER_VALIDATOR = (actionContext -> {
        final BuildingContext context = actionContext.getContext();
        final BlockPos pos = actionContext.getCurrentPos();

        BlockState worldBlockState = context.getWorldState(pos);
        TileEntity te = context.getWorld().getTileEntity(pos);

        // No air! or water
        if( worldBlockState.getMaterial() == Material.AIR || worldBlockState.getMaterial().isLiquid() )
            return false;

        // No effect blocks and don't try with the same block as you're trying to exchange with
        if (worldBlockState == OurBlocks.EFFECT_BLOCK.get().getDefaultState()
            || worldBlockState == context.getSetState() )
            return false;

        // No tiles unless construction block
        if (te != null && (!(te instanceof ConstructionBlockTileEntity) || te.getBlockState() == context.getSetState()))
            return false;

        // Don't exchange bedrock
        if (worldBlockState.getBlockHardness(context.getWorld(), pos) < 0)
            return false;

        // If fuzzy then allow to change alternative blocks in the collection, otherwise, validate it's the same state as our looking state
        if (worldBlockState.getBlock().getDefaultState() != actionContext.getStartPosState().getBlock().getDefaultState() && !context.isFuzzy())
            return false;

        // Finally, ensure at least a single face is exposed.
        // Slightly wasteful.
        boolean hasSingeValid = false;
        for(Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            BlockState state = context.getWorld().getBlockState(offset);
            if( state.isAir(context.getWorld(), offset)
                || (state.getShape(context.getWorld(), offset) != VoxelShapes.fullCube() && !(state.getBlock() instanceof StairsBlock))) {
                hasSingeValid = true;
                break;
            }
        }

        return hasSingeValid;
    });
}
