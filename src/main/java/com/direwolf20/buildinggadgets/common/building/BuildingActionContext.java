package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Hold all relevant data for the physical building / exchanging / actioning stage.
 * Mostly used to support validators
 */
public class BuildingActionContext {
    /** The overall building context */
    private final BuildingContext context;

    /** Typically run through a loop, these would be X item in that loop */
    private final BlockPos currentPos;
    private final BlockState startPosState;

    /** The building player */
    private final PlayerEntity player;

    public BuildingActionContext(BuildingContext context, BlockPos currentPos, BlockState currentState, PlayerEntity player) {
        this.context = context;
        this.currentPos = currentPos;
        this.startPosState = currentState;
        this.player = player;
    }

    public BuildingContext getContext() {
        return context;
    }

    public BlockPos getCurrentPos() {
        return currentPos;
    }

    public BlockState getStartPosState() {
        return startPosState;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
