package com.direwolf20.buildinggadgets.common.modes;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Mode {
    private final String name;
    private final boolean isExchanging;

    public Mode(String name, boolean isExchanging) {
        this.name = name;
        this.isExchanging = isExchanging;
    }

    abstract List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(ModeUseContext context, PlayerEntity player) {
        BlockPos startPos = this.withOffset(context.getStartPos(), context.getHitSide(), context.isPlaceOnTop());

        // We don't need this unless we're using the exchanger but I also don't want to
        // have to remake the state for every block.
        BlockState lookingAtState = isExchanging() ? context.getWorldState(startPos) : null;

        // We alternate the validator as the exchanger requires a more in-depth validation process.
        return collect(context, player, startPos)
                .stream()
                .filter(e -> isExchanging ? this.exchangingValidator(e, lookingAtState, context) : this.validator(player, e, context))
                .sorted(Comparator.comparing((BlockPos pos) -> player.getPosition().distanceSq(pos)))
                .collect(Collectors.toList());
    }

    /**
     * This method does the barest minimum checking that is needed by most modes
     *
     * @param context the use context instance
     * @return if the block is valid
     */
    public boolean validator(PlayerEntity player, BlockPos pos, ModeUseContext context) {
        if (!context.getWorldState(pos).isReplaceable(context.createBlockUseContext(player)))
            return false;

        if (World.isOutsideBuildHeight(pos))
            return false;

        boolean allowOverwrite = true; //Config.GENERAL.allowOverwriteBlocks.get();
        return allowOverwrite
                ? context.getWorldState(pos).getMaterial().isReplaceable()
                : context.getWorldState(pos).getMaterial() != Material.AIR;
    }

    private boolean exchangingValidator(BlockPos pos, BlockState lookingAtState, ModeUseContext context) {
        BlockState worldBlockState = context.getWorldState(pos);
        TileEntity te = context.getWorld().getTileEntity(pos);

        if ((worldBlockState != lookingAtState && !context.isFuzzy())
                //|| worldBlockState == OurBlocks.effectBlock.getDefaultState()
                || worldBlockState == context.getSetState() )
            return false;

        if (te != null
//                && (!(te instanceof ConstructionBlockTileEntity) ||
                || te.getBlockState() == context.getSetState()) //)
            return false;

        if (worldBlockState.getBlockHardness(context.getWorld(), pos) < 0)
            return false;

        if( worldBlockState.getMaterial() == Material.AIR || worldBlockState.getMaterial().isLiquid() )
            return false;

        // Finally, ensure at least a single face is exposed.
        boolean hasSingeValid = false;
        for(Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if( context.getWorld().isAirBlock(offset) || context.getWorld().getBlockState(offset).getShape(context.getWorld(), offset) != VoxelShapes.fullCube()) {
                hasSingeValid = true;
                break;
            }
        }

        return hasSingeValid;
    }

    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        return placeOnTop ? pos.offset(side, 1) : pos;
    }

    public boolean isExchanging() {
        return isExchanging;
    }

    public String getName() {
        return name;
    }
}
