package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMode {
    private boolean isExchanging;

    public AbstractMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    abstract List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(UseContext context, PlayerEntity player, Direction side) {
        BlockPos startPos = this.withOffset(context.getStartPos(), side, context.isPlaceOnTop());

        // We don't need this unless we're using the exchanger but I also don't want to
        // have to remake the state for every block.
        BlockState lookingAtState = isExchanging() ? context.getWorldState(startPos) : null;

        // We alternate the validator as the exchanger requires a more in-depth validation process.
        return collect(player, player.getPosition(), side, context.getRange(), startPos)
                .stream()
                .filter(e -> isExchanging ? this.exchangingValidator(e, lookingAtState, context) : this.validator(player, e, context))
                .collect(Collectors.toList());
    }

    /**
     * This method does the barest minimum checking that is needed by most modes
     *
     * @param context the use context instance
     * @return if the block is valid
     */
    public boolean validator(PlayerEntity player, BlockPos pos, UseContext context) {
        if (!context.getWorldState(pos).isReplaceable(context.createBlockUseContext(player)))
            return false;

        if (World.isOutsideBuildHeight(pos))
            return false;

        return Config.GENERAL.allowOverwriteBlocks.get()
                ? context.getWorldState(pos).getMaterial().isReplaceable()
                : context.getWorldState(pos).getMaterial() != Material.AIR;
    }

    private boolean exchangingValidator(BlockPos pos, BlockState lookingAtState, UseContext context) {
        BlockState worldBlockState = context.getWorldState(pos);
        TileEntity te = context.getWorld().getTileEntity(pos);

        if ((worldBlockState != lookingAtState && !context.isFuzzy())
                || worldBlockState == OurBlocks.effectBlock.getDefaultState()
                || worldBlockState == context.getSetState() )
            return false;

        if (te != null && (!(te instanceof ConstructionBlockTileEntity) || ((ConstructionBlockTileEntity) te).getBlockState() == context.getSetState()))
            return false;

        if (worldBlockState.getBlockHardness(context.getWorld(), pos) < 0)
            return false;

        if( worldBlockState.getMaterial() == Material.AIR || worldBlockState.getMaterial().isLiquid() )
            return false;

        // Finally, ensure at least a single face is exposed.
        boolean hasSingeValid = false;
        for(Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            if( context.getWorld().isAirBlock(offset) ) {
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

    public static class UseContext {
        private World world;
        private BlockState setState;
        private BlockPos startPos;

        private boolean isFuzzy;
        private boolean placeOnTop;
        private int range;
        private boolean rayTraceFluid;

        public UseContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget, boolean placeOnTop) {
            this.world = world;
            this.setState = setState;
            this.startPos = startPos;

            this.range = GadgetUtils.getToolRange(gadget);
            this.isFuzzy = AbstractGadget.getFuzzy(gadget);
            this.rayTraceFluid = AbstractGadget.shouldRayTraceFluid(gadget);

            this.placeOnTop = placeOnTop;
        }

        public UseContext(World world, BlockState setState, BlockPos startPos, ItemStack gadget) {
            this(world, setState, startPos, gadget, false);
        }

        public BlockItemUseContext createBlockUseContext(PlayerEntity player) {
            return new BlockItemUseContext(
                    new ItemUseContext(
                            player,
                            Hand.MAIN_HAND,
                            VectorHelper.getLookingAt(player, this.rayTraceFluid)
                    )
            );
        }

        public BlockState getWorldState(BlockPos pos) {
            return world.getBlockState(pos);
        }

        public World getWorld() {
            return world;
        }

        public BlockState getSetState() {
            return setState;
        }

        public boolean isFuzzy() {
            return isFuzzy;
        }

        public boolean isRayTraceFluid() {
            return rayTraceFluid;
        }

        public boolean isPlaceOnTop() {
            return placeOnTop;
        }

        public int getRange() {
            return range;
        }

        public BlockPos getStartPos() {
            return startPos;
        }
    }
}
