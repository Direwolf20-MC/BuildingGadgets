package com.direwolf20.buildinggadgets.common.items.modes;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMode {
    private boolean isExchanging;

    public AbstractMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    abstract List<BlockPos> collect(UseContext context, Player player, BlockPos start);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(UseContext context, Player player) {
        BlockPos startPos = this.withOffset(context.getStartPos(), context.getHitSide(), context.isPlaceOnTop());

        // We don't need this unless we're using the exchanger but I also don't want to
        // have to remake the state for every block.
        BlockState lookingAtState = isExchanging() ? context.getWorldState(startPos) : null;

        // We alternate the validator as the exchanger requires a more in-depth validation process.
        return collect(context, player, startPos)
                .stream()
                .filter(e -> isExchanging ? this.exchangingValidator(e, lookingAtState, context) : this.validator(player, e, context))
                .sorted(Comparator.comparing((BlockPos pos) -> player.blockPosition().distSqr(pos)))
                .collect(Collectors.toList());
    }

    /**
     * This method does the barest minimum checking that is needed by most modes
     *
     * @param context the use context instance
     * @return if the block is valid
     */
    public boolean validator(Player player, BlockPos pos, UseContext context) {
        if (!context.getWorldState(pos).canBeReplaced(context.createBlockUseContext(player)))
            return false;

        if (context.world.isOutsideBuildHeight(pos))
            return false;

        return Config.GENERAL.allowOverwriteBlocks.get()
                ? context.getWorldState(pos).getMaterial().isReplaceable()
                : context.getWorldState(pos).getMaterial() != Material.AIR;
    }

    private boolean exchangingValidator(BlockPos pos, BlockState lookingAtState, UseContext context) {
        BlockState worldBlockState = context.getWorldState(pos);
        BlockEntity te = context.getWorld().getBlockEntity(pos);

        // No air! or water
        if (worldBlockState.getMaterial() == Material.AIR || worldBlockState.getMaterial().isLiquid())
            return false;

        // No effect blocks and don't try with the same block as you're trying to exchange with
        if (worldBlockState == OurBlocks.EFFECT_BLOCK.get().defaultBlockState()
                || worldBlockState == context.getSetState())
            return false;

        // No tiles unless construction block
        if (te != null && (!(te instanceof ConstructionBlockTileEntity) || te.getBlockState() == context.getSetState()))
            return false;

        // Don't exchange bedrock
        if (worldBlockState.getDestroySpeed(context.getWorld(), pos) < 0)
            return false;

        if (worldBlockState.getBlock().defaultBlockState() != lookingAtState.getBlock().defaultBlockState() && !context.isFuzzy())
            return false;

        // Finally, ensure at least a single face is exposed.
        boolean hasSingeValid = false;
        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.relative(direction);
            BlockState state = context.getWorld().getBlockState(offset);
            if (state.isAir()
                    || (state.getShape(context.getWorld(), offset) != Shapes.block() && !(state.getBlock() instanceof StairBlock))) {
                hasSingeValid = true;
                break;
            }
        }

        return hasSingeValid;
    }

    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        return placeOnTop ? pos.relative(side, 1) : pos;
    }

    public boolean isExchanging() {
        return isExchanging;
    }

    public static class UseContext {
        private final Level world;
        private final BlockState setState;
        private final BlockPos startPos;
        private final Direction hitSide;

        private final boolean isFuzzy;
        private final boolean placeOnTop;
        private final int range;
        private final boolean rayTraceFluid;
        private final boolean isConnected;

        public UseContext(Level world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean placeOnTop, boolean isConnected) {
            this.world = world;
            this.setState = setState;
            this.startPos = startPos;

            this.range = GadgetUtils.getToolRange(gadget);
            this.isFuzzy = AbstractGadget.getFuzzy(gadget);
            this.rayTraceFluid = AbstractGadget.shouldRayTraceFluid(gadget);
            this.hitSide = hitSide;

            this.isConnected = isConnected;
            this.placeOnTop = placeOnTop;
        }

        public UseContext(Level world, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean isConnected) {
            this(world, setState, startPos, gadget, hitSide, false, isConnected);
        }

        public BlockPlaceContext createBlockUseContext(Player player) {
            return new BlockPlaceContext(
                    new UseOnContext(
                            player,
                            InteractionHand.MAIN_HAND,
                            VectorHelper.getLookingAt(player, this.rayTraceFluid)
                    )
            );
        }

        public boolean isConnected() {
            return isConnected;
        }

        public BlockState getWorldState(BlockPos pos) {
            return world.getBlockState(pos);
        }

        public Level getWorld() {
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

        public Direction getHitSide() {
            return this.hitSide;
        }

        @Override
        public String toString() {
            return "UseContext{" +
                    "world=" + world +
                    ", setState=" + setState +
                    ", startPos=" + startPos +
                    ", hitSide=" + hitSide +
                    ", isFuzzy=" + isFuzzy +
                    ", placeOnTop=" + placeOnTop +
                    ", range=" + range +
                    ", rayTraceFluid=" + rayTraceFluid +
                    '}';
        }
    }
}
