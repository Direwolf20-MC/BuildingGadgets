package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMode {
    private boolean isExchanging;

    public AbstractMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    public abstract List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start);

    /**
     * Gets the collection with filters applied stopping us having to handle the filters in the actual collection
     * method from having to handle the world etc.
     */
    public List<BlockPos> getCollection(BuildingContext context, PlayerEntity player) {
        BlockPos startPos = this.withOffset(context.getStartPos(), context.getHitSide(), context.isPlaceOnTop());

        // We don't need this unless we're using the exchanger but I also don't want to
        // have to remake the state for every block.
        BlockState lookingAtState = isExchanging() ? context.getWorldState(startPos) : null;

        // We alternate the validator as the exchanger requires a more in-depth validation process.
        return this.collect(context, player, startPos)
                .stream()
                .filter(e -> this.validate(new BuildingActionContext(context, e, lookingAtState, player)))
                .sorted((c1, c2) -> this.sorted(c1, c2, player, context))
                .collect(Collectors.toList());
    }

    /**
     * Validates using one of our prebuilt validators, intentionally abstracted out
     * to allow for custom mode validators.
     */
    public boolean validate(BuildingActionContext actionContext) {
        return this.isExchanging()
            ? BuildingValidators.EXCHANGER_VALIDATOR.test(actionContext)
            : BuildingValidators.BUILDING_VALIDATOR.test(actionContext);
    }

    public int sorted(BlockPos c1, BlockPos c2, PlayerEntity player, BuildingContext context) {
        return Double.compare(player.getPosition().distanceSq(c1), player.getPosition().distanceSq(c2));
    }

    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        return placeOnTop ? pos.offset(side, 1) : pos;
    }

    public boolean isExchanging() {
        return isExchanging;
    }

}
